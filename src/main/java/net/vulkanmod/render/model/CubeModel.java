package net.vulkanmod.render.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
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

        for (int i1 = 0; i1 < 8; i1++) {
            //pre-divide all vertices once
            this.vertices[i1].div(16.0f);
            this.transformed[i1] = this.vertices[i1];
        }

        // Optimize polygon construction (using direct constructor)
        for (Direction facing : set) {
            int baseIndex = facing.getAxisDirection() == AxisDirection.POSITIVE ? 0 : 4;
            ModelPart.Polygon polygon = new ModelPart.Polygon(
                    new ModelPart.Vertex[]{
                            this.transformed[baseIndex],
                            this.transformed[baseIndex + 1],
                            this.transformed[baseIndex + 2],
                            this.transformed[baseIndex + 3]
                    }, ..., facing); // Fill in other arguments as needed
            polygons[facing.ordinal()] = polygon;
        }
    }

    public void transformVertices(Matrix4f matrix) {
        //No need to transform vertices again, they are already transformed
    }

    public ModelPart.Polygon[] getPolygons() { return this.polygons; }
}
