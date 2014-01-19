package haven;

import haven.Defer.Future;
import haven.LocalMiniMap.MapTile;
import static haven.MCache.cmaps;
import static haven.MCache.tilesz;

import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public class CartographWindow extends Window {
    public static CartographWindow instance = null;
    
    private static final RichText.Foundry foundry = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
    private static DrawnMap drawn;
    private CheckBox gridlines;
    
    boolean rsm = false;
    private static Coord gzsz = new Coord(15,15);
    private static Coord minsz = new Coord(500,360);
    
    private class DrawnMap extends Widget {
        Coord off = new Coord();
        boolean draw_grid = false;
        
        private final Map<Coord, Future<MapTile>> pcache = new LinkedHashMap<Coord, Defer.Future<MapTile>>(9, 0.75f, true) {
            private static final long serialVersionUID = 2L;
        };
        
	private DrawnMap() {
	    super(Coord.z, CartographWindow.this.sz.sub(25,150), CartographWindow.this);
	}

        @Override
	public void draw(GOut og) {
            Coord cc = ui.gui.map.player().rc.div(tilesz);
            final Coord plg = cc.div(cmaps);
            
            Coord tc = cc.add(off);
            Coord ulg = tc.div(cmaps);
            int dy = -tc.y + (sz.y / 2);
            int dx = -tc.x + (sz.x / 2);
            while((ulg.x * cmaps.x) + dx > 0)
                ulg.x--;
            while((ulg.y * cmaps.y) + dy > 0)
                ulg.y--;

            Coord s = LocalMiniMap.bg.sz();
            for(int y = 0; (y * s.y) < sz.y; y++) {
                for(int x = 0; (x * s.x) < sz.x; x++) {
                    og.image(LocalMiniMap.bg, new Coord(x*s.x, y*s.y));
                }
            }

            GOut g = og.reclipl(new Coord(), sz);
            g.gl.glPushMatrix();

            Coord cg = new Coord();
            synchronized(pcache) {
                for(cg.y = ulg.y; (cg.y * cmaps.y) + dy < sz.y; cg.y++) {
                    for(cg.x = ulg.x; (cg.x * cmaps.x) + dx < sz.x; cg.x++) {
                        Defer.Future<MapTile> f = pcache.get(cg);
                        final Coord tcg = new Coord(cg);
                        final Coord ul = cg.mul(cmaps);
                        if((f == null) && (cg.manhattan2(plg) <= 1)) {
                            f = Defer.later(new Defer.Callable<MapTile> () {
                                public MapTile call() {
                                    BufferedImage img = LocalMiniMap.drawmap(ul, cmaps);
                                    if(img == null){return null;}
                                    return(new MapTile(new TexI(img), ul, tcg));
                                }
                            });
                            pcache.put(tcg, f);
                        }
                        if((f == null) || (!f.done())) {
                            continue;
                        }
                        MapTile mt = f.get();
                        if(mt == null){
                            pcache.put(cg, null);
                            continue;
                        }
                        Tex img = mt.img;
                        g.image(img, ul.add(tc.inv()).add(sz.div(2)));
                    }
                }
            }
            if(draw_grid)
            {
                g.chcolor(255,255,255,255);
                int startx = (g.sz.x/2 - tc.x)%cmaps.x;
                startx = startx>0?startx:startx+cmaps.x;
                for(int x = startx; x < sz.x; x+=cmaps.x)
                {
                    g.line(new Coord(x,0), new Coord(x,sz.y),1);
                }
                int starty = (g.sz.y/2 - tc.y)%cmaps.y;
                starty = starty>0?starty:starty+cmaps.y;
                for(int y = starty; y < sz.y; y+=cmaps.y)
                {
                    g.line(new Coord(0,y), new Coord(sz.x,y),1);
                }
            }
            
            g.gl.glPopMatrix();
	}
        
        @Override
        public boolean mousedown(Coord c, int button) {
            parent.setfocus(this);
            raise();

            if(button == 3){
                dm = true;
                ui.grabmouse(this);
                doff = c;
                return true;
            }

            return super.mousedown(c, button);
        }
        
        @Override
        public boolean mouseup(Coord c, int button) {
            if(button == 2){
                off.x = off.y = 0;
                return true;
            }

            if(button == 3){
                dm = false;
                ui.grabmouse(null);
                return true;
            }
           
            return super.mouseup(c, button);
        }
        
        @Override
        public void mousemove(Coord c) {
            Coord d;
            if(dm){
                d = c.sub(doff);
                off = off.sub(d);
                doff = c;
                return;
            }
                
            super.mousemove(c);
        }
    }
    
    public CartographWindow(Coord c, Widget parent) {
	super(c, new Coord(500, 360), parent, "Cartograph");
	justclose = true;
        drawn = new DrawnMap();
        
        //checkbox for the grid drawing
        gridlines = new CheckBox(new Coord(15,sz.y-100), this, "Display grid lines"){
            @Override
            public void changed(boolean val) {
                super.changed(val);
                drawn.draw_grid = val;
            }
        };
        gridlines.a = false;
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == cbtn)
	    super.wdgmsg(sender, msg, args);
    }

    public static void toggle() {
	UI ui = UI.instance;
	if(instance == null){
	    instance = new CartographWindow(ui.gui.sz.sub(500, 360).div(2), ui.gui);
	} else {
	    ui.destroy(instance);
	}
    }

    @Override
    public void destroy() {
	instance = null;
	super.destroy();
    }

    public static void close() {
	if(instance != null){
	    UI ui = UI.instance;
	    ui.destroy(instance);
	}
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
	parent.setfocus(this);
	raise();

	if (button == 1) {
	    ui.grabmouse(this);
	    doff = c;
	    if(c.isect(sz.sub(gzsz), gzsz)) {
		rsm = true;
		return true;
	    }
	}
	return super.mousedown(c, button);
    }

    @Override
    public boolean mouseup(Coord c, int button) {
	if (button == 1 && rsm){
	    ui.grabmouse(null);
	    rsm = false;
	    storeOpt("_sz", sz);
	}
        
	return super.mouseup(c, button);
    }

    @Override
    public void mousemove(Coord c) {
	Coord d;
	if (rsm){
	    d = c.sub(doff);
	    Coord newsz = sz.add(d);
	    newsz.x = Math.max(minsz.x, newsz.x);
	    newsz.y = Math.max(minsz.y, newsz.y);
	    doff = c;
            drawn.resize(newsz.sub(25,150));
            sresize(newsz);
	} else {
	    super.mousemove(c);
	}
    }
    
    private void sresize(Coord sz) {
	IBox box;
	int th;
	if(cap == null){
	    box = wbox;
	    th = 0;
	} else {
	    box = topless;
	    th = Window.th;
	}
	this.sz = sz;
	ctl = box.btloff().add(0, th);
	csz = sz.sub(box.bisz()).sub(0, th);
	atl = ctl.add(mrgn);
	asz = csz.sub(mrgn.mul(2));
	ac = new Coord();
	//ac = tlo.add(wbox.btloff()).add(mrgn);
	placetwdgs();
	for(Widget ch = child; ch != null; ch = ch.next)
	    ch.presize();
        
        gridlines.c = new Coord(15,sz.y-100);
    }
}