package haven.res.lib.point;

import haven.FastMesh;
import haven.RenderList;
import haven.Rendered;
import haven.Resource;
import haven.Sprite;

public class NFEffect extends Sprite
{
  static Resource sres = Resource.load("gfx/fx/grabb", 1);
  Rendered fx;

  public NFEffect(Sprite.Owner paramOwner)
  {
    super(paramOwner, Resource.classres(TrackEffect.class));
    FastMesh.MeshRes localMeshRes = (FastMesh.MeshRes)sres.layer(FastMesh.MeshRes.class);
    this.fx = localMeshRes.mat.get().apply(localMeshRes.m);
  }

  public boolean setup(RenderList paramRenderList) {
    paramRenderList.add(this.fx, null);
    return false;
  }
}