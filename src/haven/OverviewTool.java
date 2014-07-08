package haven;


import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class OverviewTool extends Window{
    static final String title = "Abacus";
    
    private final Label text;
    private ArrayList<Label> ls = new ArrayList<Label>();
    
    private int scalp_score = 0;
    private Map<String,Integer> artifices = new HashMap<String,Integer>();
    
    private static OverviewTool instance; 

    public OverviewTool(Coord c, Widget parent) {
	super(c, new Coord(300, 100), parent, title);
        
	this.text = new Label(Coord.z, this, "Creating overview. Please Hold...");
	toggle();
        
	this.pack();
    }

    public static OverviewTool instance(UI ui) {
	if(instance == null || instance.ui != ui){
	    instance = new OverviewTool(new Coord(100, 100), ui.gui);
	}
	return instance;
    }

    public static void close(){
	if(instance != null){
	    instance.ui.destroy(instance);
	    instance = null;
	}
    }

    public void toggle(){
        this.visible = !this.visible;
        if(this.visible)
        {
            update_artifice();
            update_scalp_score();
            update_text();
        }
    }

    public void notifyClothingChanged()
    {
        //can be used to refresh the abacus from outside
        update_artifice();
        update_text();
    }
    
    private void update_text()
    {
        String t = "Your current scalp score is "+scalp_score+".";
	this.text.settext(t);
        
        int height = 25;
        //destroy all previous labels
        for(Label l : ls)
        {
            l.destroy();
        }
        ls = new ArrayList<Label>();
        ls.add(new Label(new Coord(0,height), this, "Overview of equipped artifice:"));
        for(Entry<String,Integer> e : artifices.entrySet())
        {
            height += 15;
            ls.add(new Label(new Coord(0,height), this, "   "+e.getKey()+":"));
            ls.add(new Label(new Coord(100, height),this,"+" + e.getValue()));
        }
        
        this.pack();
    }
    
    private void update_scalp_score()
    {
        scalp_score = 0;
        Tempers tm = this.ui.gui.tm;
        for(int i : tm.lmax)
            scalp_score += 2*(i/1000-5);
        
        CharWnd cw = this.ui.gui.chrwdg;
        if(cw != null && cw.attrs != null)
        {
            for(CharWnd.Attr at : cw.attrs.values())
            {
                scalp_score+=at.attr.base-5;
            }
        }
    }
    
    private void update_artifice()
    {
        artifices = new HashMap<String,Integer>();
        
        Equipory eq = this.ui.gui.getEquipory();
        if(eq==null) return;
        //go through all slots and store the artifice values in the hashmap
        for(WItem wi : eq.slots)
        {
            if(wi != null && wi.item != null)
            {
                try{
                for(ItemInfo ii : wi.item.info())
                {
                    if(ii.getClass().getName().equals("ISlots"))
                    {
                        try{
                            //ugliest piece of code EVER! Except for FuckMeGentlyWithAChainSaw. That tops it.
                            Field f = ii.getClass().getField("s");
                            Object[] ss = (Object[]) f.get(ii);
                            for(Object o : ss)
                            {
                                if(o==null) continue;
                                
                                Field f2 = o.getClass().getField("info");
                                ArrayList<Object> infos = (ArrayList<Object>) f2.get(o);
                                for(Object inf : infos)
                                {
                                    Field attrsf = inf.getClass().getField("attrs");
                                    Field valsf = inf.getClass().getField("vals");
                                    String[] attrs = (String[]) attrsf.get(inf);
                                    int[] vals = (int[]) valsf.get(inf);
                                    for(int i = 0;i<attrs.length;i++)
                                    {
                                        if(artifices.containsKey(attrs[i]))
                                        {
                                            Integer curval = artifices.remove(attrs[i]);
                                            artifices.put(attrs[i], curval+vals[i]);
                                        }
                                        else
                                        {
                                            artifices.put(attrs[i], vals[i]);
                                        }
                                    }
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                }catch(Loading l){}
            }
        }
    }
    
    @Override
    public void destroy() {
	instance = null;
	super.destroy();
    }

    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_ESCAPE) {
	    close();
	    return true;
	}
	return super.type(key, ev);
    }

    @Override
    public void wdgmsg(Widget wdg, String msg, Object... args) {
	if (wdg == cbtn) {
	    ui.destroy(this);
	} else {
	    super.wdgmsg(wdg, msg, args);
	}
    }
}