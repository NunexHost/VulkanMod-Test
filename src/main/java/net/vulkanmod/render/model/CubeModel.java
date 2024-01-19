package net.vulkanmod.render.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Set;

public class CubeModel {

    private ModelPart.Polygon[] polygons = new ModelPart.Polygon[6];
    public float minX;
    public float minY;
    public float minZ;
    public float maxX;
    public float maxY;
    public float maxZ;

    Vector3f[] vertices;
    final Vector3f[] transformed = new Vector3f[8];

    public void setVertices(int i, int j, float f, float g, float h, float k, float l, float m, float n, float o, float p, boolean bl, float q, float r, Set<Direction> set) {

        // Pre-calculate 16.0f division

        final float div = 1.0f / 16.0f;

        this.minX = f;
        this.minY = g;
        this.minZ = h;
        this.maxX = f + k;
        this.maxY = g + l;
        this.maxZ = h + m;
        this.polygons = new ModelPart.Polygon[set.size()];

        float s = maxX;
        float t = maxY;
        float u = maxZ;
        f -= n;
        g -= o;
        h -= p;
        s += n;
        t += o;
        u += p;
        if (bl) {
            float v = s;
            s = f;
            f = v;
        }

        this.vertices = new Vector3f[]{
            new Vector3f(f, g, h),
            new Vector3f(s, g, h),
            new Vector3f(s, t, h),
            new Vector3f(f, t, h),
            new Vector3f(f, g, u),
            new Vector3f(s, g, u),
            new Vector3f(s, t, u),
            new Vector3f(f, t, u)
        };

        // Transform original vertices and store them

        for (int i1 = 0; i1 < 8; ++i1) {
            this.vertices[i1].mul(div);
            this.transformed[i1] = this.vertices[i1];
        }

        // Create vertices for each face

        ModelPart.Vertex vertex1 = new ModelPart.Vertex(this.transformed[0], 0.0F, 0.0F);
        ModelPart.Vertex vertex2 = new ModelPart.Vertex(this.transformed[1], 0.0F, 8.0F);
        ModelPart.Vertex vertex3 = new ModelPart.Vertex(this.transformed[2], 8.0F, 8.0F);
        ModelPart.Vertex vertex4 = new ModelPart.Vertex(this.transformed[3], 8.0F, 0.0F);

        // Create polygons for each face

        int idx = 0;
        if (set.contains(Direction.DOWN)) {
            this.polygons[idx++] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex1, vertex2, vertex3, vertex4}, this.transformed[0].x, this.transformed[0].y, this.transformed[0].z, this.transformed[1].x, this.transformed[1].y, this.transformed[1].z, bl, Direction.DOWN);
        }

        if (set.contains(Direction.UP)) {
            this.polygons[idx++] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex4, vertex3, vertex2, vertex1}, this.transformed[4].x, this.transformed[4].y, this.transformed[4].z, this.transformed[5].x, this.transformed[5].y, this.transformed[5].z, bl, Direction.UP);
        }
        
    }
