package net.vulkanmod.vulkan.shader.layout;

import net.vulkanmod.vulkan.shader.descriptor.UBO;

import java.util.ArrayList;
import java.util.List;

public abstract class AlignedStruct {

    protected final List<Field> fields = new ArrayList<>();
    protected final int size;

    protected AlignedStruct(List<Field.FieldInfo> infoList, int size) {
        this.size = size;

        if(infoList == null)
            return;

        for(Field.FieldInfo fieldInfo : infoList) {

            Field field = Field.createField(fieldInfo);
            this.fields.add(field);
        }

        // Sort fields by size, from largest to smallest, to minimize padding
        this.fields.sort(Field::compareSizes);
    }

    public void update(long ptr) {
        for(Field field : this.fields) {
            field.update(ptr);
        }
    }

    public List<Field> getFields() {
        return this.fields;
    }

    public int getSize() {
        return size;
    }

    public static class Builder {

        final List<Field.FieldInfo> fields = new ArrayList<>();
        protected int currentOffset = 0;

        public void addFieldInfo(String type, String name, int count) {
            Field.FieldInfo fieldInfo = Field.createFieldInfo(type, name, count);

            // Pre-compute alignment offset
            fieldInfo.alignmentOffset = fieldInfo.computeAlignmentOffset(currentOffset);

            // Update current offset
            currentOffset = fieldInfo.alignmentOffset + fieldInfo.size;

            fields.add(fieldInfo);
        }

        public void addFieldInfo(String type, String name) {
            Field.FieldInfo fieldInfo = Field.createFieldInfo(type, name);

            // Pre-compute alignment offset
            fieldInfo.alignmentOffset = fieldInfo.computeAlignmentOffset(currentOffset);

            // Update current offset
            currentOffset = fieldInfo.alignmentOffset + fieldInfo.size;

            fields.add(fieldInfo);
        }

        public UBO buildUBO(int binding, int stages) {
            //offset is expressed in floats/ints
            return new UBO(binding, stages, currentOffset * 4, fields);
        }

        public PushConstants buildPushConstant() {
            if(fields.isEmpty()) return null;
            return new PushConstants(fields, currentOffset * 4);
        }

    }

}
