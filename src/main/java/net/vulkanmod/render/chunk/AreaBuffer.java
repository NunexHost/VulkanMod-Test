package net.vulkanmod.render.chunk;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.vulkanmod.render.chunk.util.Util;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.vulkan.memory.*;
import net.vulkanmod.vulkan.queue.TransferQueue;
import org.apache.commons.lang3.Validate;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

public class AreaBuffer {
    private final MemoryType memoryType;
    private final int usage;

    private final LinkedList<Segment> freeSegments = new LinkedList<>();
    private final ObjectArrayList<virtualSegment> usedSegments = new ObjectArrayList<>();

    private final int elementSize;

    private Buffer buffer;

    int size;
    int used;

    public AreaBuffer(int usage, int size, int elementSize) {

        this.usage = usage;
        this.elementSize = elementSize;
        this.memoryType = MemoryTypes.GPU_MEM;

        this.buffer = this.allocateBuffer(size);
        this.size = size;

        freeSegments.add(new Segment(0, size));
    }

    private Buffer allocateBuffer(int size) {

        return this.usage == VK_BUFFER_USAGE_VERTEX_BUFFER_BIT ? new VertexBuffer(size, memoryType) : new IndexBuffer(size, memoryType);
    }

    public synchronized virtualSegment upload(ByteBuffer byteBuffer, TerrainRenderType r, virtualSegment uploadSegment, int index) {

        int size = byteBuffer.remaining();

        if(size % elementSize != 0)
            throw new RuntimeException("unaligned byteBuffer");


        final virtualSegment v;
        if(uploadSegment==null || !this.isAlreadyLoaded(index, size, r))
        {
            v = findSegment(size, index, r);
            usedSegments.add(v);
            this.used += v.size();
        }
        else
        {
            v = uploadSegment;
        }





//        if(segment.size() - size > 0) {
//            freeSegments.add(new virtualSegment(index, segment.offset() + size, segment.size() - size));
//        }
//
//        final virtualSegment v = new virtualSegment(index, segment.offset(), size);

        Validate.isTrue(v.offset() >=0);
        Validate.isTrue(v.size() > 0);
        Validate.isTrue(v.offset() < this.buffer.getBufferSize());
        Validate.isTrue(v.size() < this.buffer.getBufferSize());

        AreaUploadManager.INSTANCE.uploadAsync(v, this.buffer.getId(), v.offset(), size, byteBuffer);

//        uploadSegment.offset() = segment.offset();
//        uploadSegment.size = size;
//        uploadSegment.status = Segment.PENDING_BIT;


        return v;
    }

//    private void removeSegment(int index) {
//        var a = usedSegments.remove(index);
//        this.used-= a!=null ? a.size() : 0;
//    }

    //Allows segment suballocations to be skipped if the size is equal to or smaller: Should help to reduce fragmentation
    private boolean isAlreadyLoaded(int index, int size1, TerrainRenderType r) {
        for(var a : usedSegments)
        {
            if(a.subIndex() == index && a.size() >= size1 && a.r()==r)
            {
                return true;
            }

        }
        setSegmentFree(index, r);
        return false;
    }

    public virtualSegment findSegment(int size, int index, TerrainRenderType r) {
        //        if(freeSegments.isEmpty()) return new virtualSegment(index, 0, size);
        for (int i = 0; i < freeSegments.size(); i++) {
            Segment segment1 = freeSegments.get(i);
            if (segment1.size >= size) {
                virtualSegment segment = new virtualSegment(index, segment1.offset, size, r);
                if(segment1.size-size>0) freeSegments.set(i, new Segment(segment.offset() + size, segment1.size - size));
                else freeSegments.remove(i);
                return segment;
            }
        }

        return this.reallocate(size, index, r);


    }

    public virtualSegment reallocate(int uploadSize, int index, TerrainRenderType r) {
        int oldSize = this.size;
        int increment = this.size >> 1;

        if(increment <= uploadSize) {
            increment *= 2;
        }
        //TODO check size
        if(increment <= uploadSize)
            throw new RuntimeException();

        int newSize = oldSize + increment;


        Buffer buffer = this.allocateBuffer(newSize);

//        AreaUploadManager.INSTANCE.submitUploads();
        AreaUploadManager.INSTANCE.copy(this.buffer, buffer);
//        AreaUploadManager.INSTANCE.waitUploads(); //TODO: Check if this Optimisation is overaggressive and causes miss-alignments

        //Sync upload
        this.buffer.freeBuffer();
        this.buffer = buffer;

        this.size = newSize;

        int offset = Util.align(oldSize, elementSize);

        virtualSegment segment = new virtualSegment(index, offset, uploadSize, r);

        freeSegments.add(new Segment(offset + segment.size(), increment - segment.size()));
        return segment;
    }

    public synchronized void setSegmentFree(int k, TerrainRenderType r) {
        for (int i = 0; i < usedSegments.size(); i++) {
            var a = usedSegments.get(i);
            if (a.subIndex() == k && a.r() == r) {
                var segment = usedSegments.remove(i);
                this.freeSegments.add(new Segment(segment.offset(), segment.size()));
                this.used -= segment.size();
                break;
            }

        }


    }

    public long getId() {
        return this.buffer.getId();
    }

    public void freeBuffer() {
        this.buffer.freeBuffer();
        this.freeSegments.clear();
        this.usedSegments.clear();
//        this.globalBuffer.freeSubAllocation(subAllocation);
    }

    public record Segment(int offset, int size) {}

//    //Debug
//    public List<Segment> findConflicts(int offset) {
//        List<Segment> segments = new ArrayList<>();
//        Segment segment = this.usedSegments.get(offset);
//
//        for(Segment s : this.usedSegments.values()) {
//            if((s.offset() >= segment.offset() && s.offset() < (segment.offset() + segment.size))
//              || (segment.offset() >= s.offset() && segment.offset() < (s.offset() + s.size))) {
//                segments.add(s);
//            }
//        }
//
//        return segments;
//    }

//    public static boolean checkRanges(Segment s1, Segment s2) {
//        return (s1.offset() >= s2.offset() && s1.offset() < (s2.offset() + s2.size)) || (s2.offset() >= s1.offset() && s2.offset() < (s1.offset() + s1.size));
//    }
//
//    public Segment getSegment(int offset) {
//        return this.usedSegments.get(offset);
//    }
}
