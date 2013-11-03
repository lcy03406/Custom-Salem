package haven.res.lib.point;

import haven.Config;
import haven.Coord3f;
import haven.FastMesh;
import haven.Gob;
import haven.Location;
import haven.Material;
import haven.RenderList;
import haven.Rendered;
import haven.Resource;
import haven.Sprite;
import java.awt.Color;

public class TrackEffect extends Sprite
{
  static Resource sres = Resource.load("gfx/fx/arrow", 1);
  Rendered fx;
  public double a1, lasta1;
  public double a2, lasta2;
  double ca;
  double oa;
  double da;
  double t;
  double tt;

  public TrackEffect(Sprite.Owner paramOwner, double paramDouble1, double paramDouble2)
  {
    super(paramOwner, sres);
    this.a1 = paramDouble1;
    this.a2 = paramDouble2;
    this.ca = ((paramDouble1 + paramDouble2) / 2.0D);
    this.t = (this.tt = 0.0D);
    FastMesh.MeshRes localMeshRes = (FastMesh.MeshRes)sres.layer(FastMesh.MeshRes.class);
    this.fx = localMeshRes.mat.get().apply(localMeshRes.m);
  }

  public boolean tick(int paramInt) {
    double d1 = paramInt / 1000.0D;
    this.t += d1;
    if ( this.t > this.tt || Config.altprosp) {
      this.oa = this.ca;
      double d2 = this.a1 + Math.random() * (this.a2 - this.a1);
      this.da = (d2 - this.oa);
      if (this.da > 3.141592653589793D)
        this.da -= 6.283185307179586D;
      this.t = 0.0D;
      this.tt = Math.max(Math.min(Math.abs(this.oa - d2), 0.3D), 0.1D);
    }
    
    this.ca = (this.oa + this.da * (this.t / this.tt));
        if(a1 != lasta1 || a2 != lasta2)
        {
            System.out.println("From "+a1+" to "+a2);
            lasta1 = a1;
            lasta2 = a2;
        }
    return false;
  }

  public boolean setup(RenderList paramRenderList) {
    if(Config.altprosp)
    {
        paramRenderList.add(this.fx, Location.compose(Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.a1)), Location.xlate(new Coord3f(5,0,0))));
        paramRenderList.add(this.fx, Location.compose(Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - (this.a1+this.a2)/2)), Location.xlate(new Coord3f(5,0,1)), new Material.Colors(Color.GREEN)));
        paramRenderList.add(this.fx, Location.compose(Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.a2)), Location.xlate(new Coord3f(5,0,0))));
    }
    else
    {
        paramRenderList.add(this.fx, Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.ca)));
    }
    return false;
  }
}