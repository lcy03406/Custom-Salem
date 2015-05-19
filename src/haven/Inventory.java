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

import java.util.*;

import com.google.common.collect.*;

public class Inventory extends Widget implements DTarget {
    private static final Tex obt = Resource.loadtex("gfx/hud/inv/obt");
    private static final Tex obr = Resource.loadtex("gfx/hud/inv/obr");
    private static final Tex obb = Resource.loadtex("gfx/hud/inv/obb");
    private static final Tex obl = Resource.loadtex("gfx/hud/inv/obl");
    private static final Tex ctl = Resource.loadtex("gfx/hud/inv/octl");
    private static final Tex ctr = Resource.loadtex("gfx/hud/inv/octr");
    private static final Tex cbr = Resource.loadtex("gfx/hud/inv/ocbr");
    private static final Tex cbl = Resource.loadtex("gfx/hud/inv/ocbl");
    private static final Tex bsq = Resource.loadtex("gfx/hud/inv/sq");
    public static final Coord sqsz = bsq.sz();
    public static final Coord isqsz = new Coord(40, 40);
    public static final Tex sqlite = Resource.loadtex("gfx/hud/inv/sq1");
    public static final Coord sqlo = new Coord(4, 4);
    public static final Tex refl = Resource.loadtex("gfx/hud/invref");

    private static final Comparator<WItem> cmp_asc = new WItemComparator();
    private static final Comparator<WItem> cmp_desc = new Comparator<WItem>() {
	@Override
	public int compare(WItem o1, WItem o2) {
	    return cmp_asc.compare(o2, o1);
	}
    };
    private static final Comparator<WItem> cmp_name = new Comparator<WItem>() {
	@Override
	public int compare(WItem o1, WItem o2) {
            try{
        	int result = o1.item.name().compareTo(o2.item.name());
        	if(result == 0)
        	{
        	    result = cmp_desc.compare(o1, o2);
        	}
        	return result;
            }catch(Loading l){return 0;}
        }
    };
    private static final Comparator<WItem> cmp_gobble = new Comparator<WItem>() {
	@Override
	public int compare(WItem o1, WItem o2) {
            try{
        	GobbleInfo g1 = ItemInfo.find(GobbleInfo.class, o1.item.info());
        	GobbleInfo g2 = ItemInfo.find(GobbleInfo.class, o2.item.info());
        	if (g1 == null && g2 == null)
        	    return cmp_name.compare(o1, o2);
        	else if (g1 == null)
        	    return 1;
        	else if (g2 == null)
        	    return -1;
        	int v1 = g1.mainTemper();
        	int v2 = g2.mainTemper();
        	if (v1 == v2)
        	    return cmp_name.compare(o1, o2);
        	return v2-v1;
            }catch(Loading l){return 0;}
        }
    };

    Coord isz,isz_client;
    Map<GItem, WItem> wmap = new HashMap<GItem, WItem>();
    public int newseq = 0;

    @RName("inv")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    return(new Inventory(c, (Coord)args[0], parent));
	}
    }

    public void draw(GOut g) {
	invsq(g, Coord.z, isz_client);
	for(Coord cc = new Coord(0, 0); cc.y < isz_client.y; cc.y++) {
	    for(cc.x = 0; cc.x < isz_client.x; cc.x++) {
		invrefl(g, sqoff(cc), isqsz);
	    }
	}
	super.draw(g);
    }

    BiMap<Coord,Coord> dictionaryClientServer;
    boolean isTranslated = false;
    public Inventory(Coord c, Coord sz, Widget parent) {
	super(c, invsz(sz), parent);
	isz = sz;
        isz_client = sz;
        
        if(sz.equals(new Coord(1,1))|| !Window.class.isInstance(parent))
        {
            return;
        }
        dictionaryClientServer = HashBiMap.create();
        
        IButton sbtn = new IButton(Coord.z, parent, Window.obtni[0], Window.obtni[1], Window.obtni[2]){
            {tooltip = Text.render("Sort the items in this inventory by name.");}

            @Override
            public void click() {
                if(this.ui != null)
                {
                    Inventory.this.sortItemsLocally(cmp_name);
                }
            }
        };
        sbtn.visible = true;
        ((Window)parent).addtwdg(sbtn);
        IButton sgbtn = new IButton(Coord.z, parent, Window.gbtni[0], Window.gbtni[1], Window.gbtni[2]){
            {tooltip = Text.render("Sort the items in this inventory by gobble.");}

            @Override
            public void click() {
                if(this.ui != null)
                {
                    Inventory.this.sortItemsLocally(cmp_gobble);
                }
            }
        };
        sgbtn.visible = true;
        ((Window)parent).addtwdg(sgbtn);
        IButton nsbtn = new IButton(Coord.z, parent, Window.lbtni[0], Window.lbtni[1], Window.lbtni[2]){
            {tooltip = Text.render("Undo client-side sorting.");}

            @Override
            public void click() {
                if(this.ui != null)
                {
                    Inventory.this.removeDictionary();
                }
            }
        };
        nsbtn.visible = true;
        ((Window)parent).addtwdg(nsbtn);
        if (parent != ui.gui.invwnd) {
            BotHelper.box = this;
            BotHelper.wake("box_open");
        }
    }
    
    public void destroy() {
	if (this == BotHelper.box) {
	    BotHelper.box = null;
	    BotHelper.wake("box_close");
	}
	super.destroy();
    }

    public void sortItemsLocally(Comparator<WItem> comp)
    {
        isTranslated = true;
        //first step: deciding the size of the sorted inventory
        int width = this.isz.x;
        //int height = this.isz.y;
        if(this.equals(this.ui.gui.maininv))
        {
            //flexible size
            int nr_items = wmap.size();
            float aspect_ratio = 8/4;
            width  = Math.max(4,(int) Math.ceil(Math.sqrt(aspect_ratio*nr_items)));
            //height = Math.max(4,(int) Math.ceil(nr_items/width));
        }
        //now sort the item array
        List<WItem> array = new ArrayList<WItem>(wmap.values());
        Collections.sort(array, comp);
        //assign the new locations to each of the items and add new translations
        int index = 0;
        BiMap<Coord,Coord> newdictionary = HashBiMap.create();
        for(WItem w : array)
        {
            Coord newclientloc = new Coord((index%(width)),(int)(index/(width)));
            
            //adding the translation to the dictionary
            Coord serverloc = w.server_c;
            newdictionary.put(newclientloc,serverloc);

            //moving the widget to its ordered place
            w.c = sqoff(newclientloc);
            
            //on to the next location
            index++;
        }
        dictionaryClientServer = newdictionary;
        
        //resize the inventory to the new set-up
        this.updateClientSideSize();
    }
    
    public Coord translateCoordinatesClientServer(Coord client)
    {
        if(!isTranslated)
            return client;
        Coord server = client;
        if(dictionaryClientServer.containsKey(client))
        {
            server = dictionaryClientServer.get(client);
        }
        else if(dictionaryClientServer.containsValue(client))
        {
            //i.e. we don't have an item there but the server does: find a solution!
            int width = isz.x;
            //int height = isz.y;
            int index = 0;
            Coord newloc;
            do{
                newloc = new Coord((index%(width-1)),(int)(index/(width-1)));
                index++;
            }while(dictionaryClientServer.containsValue(newloc));
            server = newloc;
            dictionaryClientServer.put(client,server);
        }
        return server;
    }
    
    public Coord translateCoordinatesServerClient(Coord server)
    {
        if(!isTranslated)
            return server;
        Coord client = server;
        BiMap<Coord,Coord> dictionaryServerClient = dictionaryClientServer.inverse();
        if(dictionaryServerClient.containsKey(server))
        {
            client = dictionaryServerClient.get(server);
        }
        else{
            //find a spot for it
            int width = isz_client.x;
            //int height = isz_client.y;
            int index = 0;
            Coord newloc;
            do{
                newloc = new Coord((index%(width-1)),(int)(index/(width-1)));
                index++;
            }while(dictionaryClientServer.containsKey(newloc));
            client = newloc;
            dictionaryClientServer.put(client,server);
        }
        return client;
    }
    
    public void removeDictionary()
    {
        isTranslated = false;
        dictionaryClientServer = HashBiMap.create();
        for(WItem w : wmap.values())
        {
            w.c = sqoff(w.server_c);
        }
        this.updateClientSideSize();
    }
    
    public void sanitizeDictionary()
    {
        if(wmap.isEmpty())
        {
            removeDictionary();
        }
    }
    
    public Coord updateClientSideSize()
    {
        if(this.equals(ui.gui.maininv))
        {
            int maxx = 2;
            int maxy = 2;
            for(WItem w : wmap.values())
            {
                Coord wc = sqroff(w.c);
                maxx = Math.max(wc.x,maxx);
                maxy = Math.max(wc.y,maxy);
            }
            this.isz_client = new Coord(maxx+2,maxy+2);
            this.resize(invsz(isz_client));
            return isz_client;
        }
        else
        {
            return isz_client = isz;
        }
    }
    
    public static Coord sqoff(Coord c) {
	return(c.mul(sqsz).add(ctl.sz()));
    }

    public static Coord sqroff(Coord c) {
	return(c.sub(ctl.sz()).div(sqsz));
    }

    public static Coord invsz(Coord sz) {
	return(sz.mul(sqsz).add(ctl.sz()).add(cbr.sz()).sub(4, 4));
    }

    public static void invrefl(GOut g, Coord c, Coord sz) {
	Coord ul = g.ul.sub(g.ul.div(2)).mod(refl.sz()).inv();
	Coord rc = new Coord();
	for(rc.y = ul.y; rc.y < c.y + sz.y; rc.y += refl.sz().y) {
	    for(rc.x = ul.x; rc.x < c.x + sz.x; rc.x += refl.sz().x) {
		g.image(refl, rc, c, sz);
	    }
	}
    }

    public static void invsq(GOut g, Coord c, Coord sz) {
	for(Coord cc = new Coord(0, 0); cc.y < sz.y; cc.y++) {
	    for(cc.x = 0; cc.x < sz.x; cc.x++) {
		g.image(bsq, c.add(cc.mul(sqsz)).add(ctl.sz()));
	    }
	}
	for(int x = 0; x < sz.x; x++) {
	    g.image(obt, c.add(ctl.sz().x + sqsz.x * x, 0));
	    g.image(obb, c.add(ctl.sz().x + sqsz.x * x, obt.sz().y + (sqsz.y * sz.y) - 4));
	}
	for(int y = 0; y < sz.y; y++) {
	    g.image(obl, c.add(0, ctl.sz().y + sqsz.y * y));
	    g.image(obr, c.add(obl.sz().x + (sqsz.x * sz.x) - 4, ctl.sz().y + sqsz.y * y));
	}
	g.image(ctl, c);
	g.image(ctr, c.add(ctl.sz().x + (sqsz.x * sz.x) - 4, 0));
	g.image(cbl, c.add(0, ctl.sz().y + (sqsz.y * sz.y) - 4));
	g.image(cbr, c.add(cbl.sz().x + (sqsz.x * sz.x) - 4, ctr.sz().y + (sqsz.y * sz.y) - 4));
    }

    public static void invsq(GOut g, Coord c) {
	g.image(sqlite, c);
    }

    public boolean mousewheel(Coord c, int amount) {
        if(ui.modshift) {
            wdgmsg("xfer", amount);
        }
        return(true);
    }
    public Widget makechild(String type, Object[] pargs, Object[] cargs) {
    	Coord server_c = (Coord)pargs[0];
        Coord c = translateCoordinatesServerClient(server_c);
	Widget ret = gettype(type).create(c, this, cargs);
	if(ret instanceof GItem) {
	    GItem i = (GItem)ret;
	    wmap.put(i, new WItem(sqoff(c), this, i, server_c));
	    newseq++;
            
            if(isTranslated)
                updateClientSideSize();
	}
	return(ret);
    }

    public void cdestroy(Widget w) {
	super.cdestroy(w);
	if(w instanceof GItem) {
	    GItem i = (GItem)w;
            WItem wi = wmap.remove(i);
            if(isTranslated)
            {
                sanitizeDictionary();
            }
	    ui.destroy(wi);
	}
    }

    public boolean drop(Coord cc, Coord ul) {
        Coord clientcoords = sqroff(ul.add(isqsz.div(2)));
        Coord servercoords = translateCoordinatesClientServer(clientcoords);
	wdgmsg("drop", servercoords);
	return(true);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }

    public void uimsg(String msg, Object... args) {
	if(msg.equals("sz")) {
	    isz = (Coord)args[0];
            if(isTranslated)
            {
                resize(invsz(updateClientSideSize()));
            }
            else
            {
                isz_client = isz;
                resize(invsz(isz));
            }
	}
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(msg.equals("transfer-same")){
	    process(getSame((String) args[0],(Boolean)args[1]), "transfer");
	} else if(msg.equals("drop-same")){
	    process(getSame((String) args[0], (Boolean) args[1]), "drop");
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    private void process(List<WItem> items, String action) {
	for (WItem item : items){
	    item.item.wdgmsg(action, Coord.z);
	}
    }

    private List<WItem> getSame(String name, Boolean ascending) {
	List<WItem> items = new ArrayList<WItem>();
	for (Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
	    if (wdg.visible && wdg instanceof WItem) {
		String iname = ((WItem) wdg).item.name();
		if (BotHelper.matchName(iname, name))
		    items.add((WItem) wdg);
	    }
	}
	Collections.sort(items, ascending?cmp_asc:cmp_desc);
	return items;
    }
    
    public GItem findItem(String name) {
	List<WItem> l = getSame(name, false);
	if (l.isEmpty())
	    return null;
	return l.get(0).item;
    }
    
    public Coord findEmpty() {
	sortItemsLocally(cmp_name);
        int width = isz.x;
        int height = isz.y;
        if(this.equals(this.ui.gui.maininv))
            width --;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
        	Coord c = new Coord(x, y);
        	if (!dictionaryClientServer.containsValue(c))
        	    return c;
            }
        }
        return null;
    }

    public ArrayList<GItem> allItem(String name) {
	List<WItem> l = getSame(name, false);
	if (l.isEmpty())
	    return null;
	ArrayList<GItem> a = new ArrayList<GItem>();
	for (WItem i : l) {
	    a.add(i.item);
	}
	return a;
    }
}
