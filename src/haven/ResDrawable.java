/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

public class ResDrawable extends Drawable {
    final public  Indir<Resource> res;
    Message sdt;
    int mask;
    Sprite spr = null;
    int delay = 0;
    boolean show_radius = false;
    Gob.Overlay radius;
	
    public ResDrawable(Gob gob, Indir<Resource> res, Message sdt) {
	super(gob);
	this.res = res;
	setData(sdt);
	this.sdt = sdt;
	this.mask = sdt.eom()?0xffff0000:Sprite.decnum(sdt.clone());
	try {
	    init();
	} catch(Loading ignored) {}
    }
	
    public ResDrawable(Gob gob, Resource res) {
	this(gob, res.indir(), new Message(0));
    }
	
    public void init() {
	if(spr != null)
	    return;
	spr = Sprite.create(gob, res.get(), sdt.clone());

	String name = res.get().name;
	if(Config.item_radius.containsKey(name)){
	    ColoredRadius.Cfg cfg = Config.item_radius.get(name);
	    radius = new Gob.Overlay(new ColoredRadius(gob, cfg));
	}
    }
    
    public void setData(Message sdt) {
	this.sdt = sdt;
	this.mask = sdt.eom()?0xffff0000:Sprite.decnum(sdt.clone());
    }
	
    public void setup(RenderList rl) {
	try {
	    init();
	} catch(Loading e) {
	    return;
	}
	checkRadius();
	spr.setup(rl);
    }

    private void checkRadius() {
	if(radius != null && show_radius != Config.show_radius){
	    show_radius = Config.show_radius;
	    gob.ols.remove(radius);
	    if(show_radius){
		gob.ols.add(radius);
	    }
	}
    }

    public void ctick(int dt) {
	if(spr == null) {
	    delay += dt;
	} else {
	    spr.tick(delay + dt);
	    delay = 0;
	}
    }
    
    public void dispose() {
	if(spr != null)
	    spr.dispose();
    }
    
    public Resource.Neg getneg() {
	return(res.get().layer(Resource.negc));
    }
    
    public Skeleton.Pose getpose() {
	init();
	if(spr instanceof SkelSprite) {
	    return(((SkelSprite)spr).pose);
	}
	return(null);
    }
}
