package haven.res.lib;

import haven.*;
import haven.Material.Colors;

import java.awt.*;

public class HomeTrackerFX extends Sprite {
    private static final Location SCALE = Location.scale(new Coord3f(1.2f, 1.2f ,1));
    private static final Colors COLORS = new Material.Colors(Color.GREEN);
    private static final Location XLATE = Location.xlate(new Coord3f(0,0,2.5f));
    private static final Location SCALEA = Location.scale(new Coord3f(2.0f, 2.0f ,1));
    private static final Colors COLORSA = new Material.Colors(Color.GRAY);
    private static final Location XLATEA = Location.xlate(new Coord3f(2.0f,0,2.0f));
    static Resource sres = Resource.load("gfx/fx/arrow", 1);
    Rendered fx = null;
    Rendered fxx = null;
    double ca = 0;
    Gob.Overlay curol = null;
    public Coord c = null;

    public HomeTrackerFX(Owner owner) {
	super(owner, sres);
	((Gob)owner).ols.add(curol = new Gob.Overlay(this));
    }

    
    
    @Override
    public boolean setup(RenderList d) {

	if( !Config.hptr || UI.instance == null || !Config.hpointv ){
	    return false;
	}
	if(fx == null){
	    FastMesh.MeshRes mres = (FastMesh.MeshRes)sres.layer(FastMesh.MeshRes.class);
	    this.fx = mres.mat.get().apply(mres.m);
	    this.fxx = mres.mat.get().apply(mres.m);
	}
	if(c != null && ((Gob)owner).rc != null){
	    Location rot = Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.ca));
	    d.add(this.fx, GLState.compose(XLATE, SCALE, COLORS, rot));
	    Location rotx = Location.rot(Coord3f.zu, (float)((Gob)this.owner).a);
	    d.add(this.fxx, GLState.compose(XLATEA, SCALEA, COLORSA, rotx));
	}
	return false;
    }
    
    @Override
    public boolean tick(int dt) {
	if(c != null && ((Gob)owner).rc != null){
	    ca = ((Gob)owner).rc.angle(c);
	}
	
	return false;
    }
    
    @Override
    public void dispose() {
	super.dispose();
	((Gob)owner).ols.remove(curol);
    }

    public static class HTrackWdg extends Widget{
	private Widget ptr;
	private HomeTrackerFX fx;
	private Gob player = null;
	private Coord hc;

	public HTrackWdg(Widget parent, Widget ptr) {
	    super(Coord.z, Coord.z, parent);
	    this.ptr = ptr;
	}

	@Override
	public void uimsg(String msg, Object... args) {
	    if(msg.equals("upd")){
		Coord hc = (Coord) args[0];
		this.hc = hc;
		if(fx != null){fx.c = hc;}
	    }
	    ptr.uimsg(msg, args);
	}

	@Override
	public void draw(GOut g) {
	    super.draw(g);
	    
	    Gob gob = ui.sess.glob.oc.getgob(ui.gui.plid);
	    if(gob != player){
		player = gob;
		if(fx != null){
		    fx.dispose();
		    fx = null;
		}
		if(player != null){
		    fx = new HomeTrackerFX(player);
		    fx.c = hc;
		}
	    }
	}

        public void dispose(){
            if(fx!=null)
                fx.dispose();
            if(ptr!=null)
                ptr.destroy();
        }
    }

}
