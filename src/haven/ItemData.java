package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import haven.Glob.Pagina;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ItemData {
    private static Gson gson;
    private static Map<String, ItemData> item_data = new LinkedHashMap<String, ItemData>(9, 0.75f, true) {
	private static final long serialVersionUID = 1L;

	protected boolean removeEldestEntry(Map.Entry<String, ItemData> eldest) {
	    return size() > 75;
	}

    };

    ArrayList<Data> data = new ArrayList<Data>();
    public VariantsInfo.Data variants;
    private class Data implements Comparable<Data>
    {
        public String name;
        public List<String> adhoc = new ArrayList<String>();
        public double purity;
        public FoodInfo.Data food;
        public Inspiration.Data inspiration;
        public GobbleInfo.Data gobble;
        public ArtificeData artifice;
        public int uses;
 
        public Data(GItem item){
            this(item.info());
        }
    
        public Data(List<ItemInfo> info) {
            init(info);
        }

        public void init(List<ItemInfo> info) {
            double multiplier = getMultiplier(info);
            uses = getUses(info);
            for(ItemInfo ii : info){
        	String className = ii.getClass().getCanonicalName();
        	if(ii instanceof ItemInfo.Name){
        	    name = ((ItemInfo.Name) ii).str.text;
        	} else if(ii instanceof Alchemy){
        	    purity = ((Alchemy) ii).purity();
        	} else if(ii instanceof ItemInfo.AdHoc){
        	    adhoc.add(((ItemInfo.AdHoc) ii).str.text);
        	} else if(ii instanceof FoodInfo){
        	    food = new FoodInfo.Data((FoodInfo) ii, multiplier);
        	} else if(ii instanceof Inspiration){
        	    inspiration = new Inspiration.Data((Inspiration) ii);
        	} else if(ii instanceof GobbleInfo){
        	    gobble = new GobbleInfo.Data((GobbleInfo) ii, multiplier);
        	} else if(className.equals("Slotted")){
        	    artifice = new ArtificeData(ii);
        	}
            }
        }
        
        public BufferedImage longtip() {
            String tt = String.format("%s\npurity:%.2f", name, purity);
            for (String hoc : adhoc)
            {
        	tt += "\n" + hoc; 
            }
            BufferedImage img = MenuGrid.ttfnd.render(tt, 300).img;
            ITipData[] data = new ITipData[]{food, gobble, inspiration, artifice};
            for(ITipData tip : data) {
        	if (tip != null) {
        	    img = ItemInfo.catimgs(3, img, tip.create().longtip());
        	}
            }
            if(uses > 0){
        	img = ItemInfo.catimgs(3, img, RichText.stdf.render(String.format("$b{$col[192,192,64]{Uses: %d}}\n", uses)).img);
            }
            return img;
        }

	@Override
	public int compareTo(Data arg1) {
	    int ret = name.compareTo(arg1.name);
	    if (ret != 0)
		return ret;
	    ret = adhoc.size() - arg1.adhoc.size();
	    if (ret != 0)
		return ret;
	    for (int i = 0; i < adhoc.size(); i++)
	    {
		ret = adhoc.get(i).compareTo(arg1.adhoc.get(i));
		if (ret != 0)
		    return ret;
	    }
	    return ret;
	}
    };
    
    public void update(GItem item)
    {
	Data n = new Data(item);
	for (Data d : data)
	{
	    if (0 == d.compareTo(n))
	    {
		data.remove(d);
	    }
	}
	data.add(n);
	Collections.sort(data);
    }

    public Tex longtip(Resource res, int idx) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt = String.format("%s (%d of %d)\n\n", ad.name, idx, data.size());
	if(pg != null){tt += pg.text;}

	BufferedImage img = MenuGrid.ttfnd.render(tt, 300).img;
	if (idx > 0 && idx <= data.size())
	{
	    Data d = data.get(idx - 1);
	    img = ItemInfo.catimgs(3, img, d.longtip());
	}
	else
	{
	    for (Data d : data)
	    {
		img = ItemInfo.catimgs(3, img, d.longtip());
	    }
	}
	return new TexI(img);
    }
    
    public static interface ITipData {
	ItemInfo.Tip create();
    }
    
    public static void actualize(GItem item, Pagina pagina) {
	String name = item.name();
	if(name == null){ return; }

	name = pagina.res().name;
	ItemData data = item_data.get(name);
	if (data == null)
	    data =  new ItemData();
	data.update(item);
	item_data.put(name, data);
	store(name, data);
    }

    private static int getUses(List<ItemInfo> info) {
	GItem.NumberInfo ninf = ItemInfo.find(GItem.NumberInfo.class, info);
	if(ninf != null){
	    return ninf.itemnum();
	}
	return -1;
    }

    private static double getMultiplier(List<ItemInfo> info) {
	Alchemy alch = ItemInfo.find(Alchemy.class, info);
	if(alch != null){
	    return 1+alch.purity();
	}
	return 1;
    }

    private static void store(String name, ItemData data) {
	File file = Config.getFile(getFilename(name));
	boolean exists = file.exists();
	if(!exists){
	    try {
		//noinspection ResultOfMethodCallIgnored
		new File(file.getParent()).mkdirs();
		exists = file.createNewFile();
	    } catch (IOException ignored) {}
	}
	if(exists && file.canWrite()){
	    PrintWriter out = null;
	    try {
		out = new PrintWriter(file);
		out.print(getGson().toJson(data));
	    } catch (FileNotFoundException ignored) {
	    } finally {
		if (out != null) {
		    out.close();
		}
	    }
	}
    }

    public static ItemData get(String name) {
	if(item_data.containsKey(name)){
	    return item_data.get(name);
	}
	return load(name);
    }

    private static ItemData load(String name) {
	ItemData data = null;
	String filename = getFilename(name);
	InputStream inputStream = null;
	File file = Config.getFile(filename);
	if(file.exists() && file.canRead()) {
	    try {
		inputStream = new FileInputStream(file);
	    } catch (FileNotFoundException ignored) {
	    }
	} else {
	    inputStream = ItemData.class.getResourceAsStream(filename);
	}
	if(inputStream != null) {
	    data = parseStream(inputStream);
	    item_data.put(name, data);
	}
	return data;
    }

    private static String getFilename(String name) {
	return "/item_data/" + name + ".json";
    }

    private static ItemData parseStream(InputStream inputStream) {
	ItemData data = null;
	try {
	    String json = Utils.stream2str(inputStream);
	    data =  getGson().fromJson(json, ItemData.class);
	} catch (JsonSyntaxException ignore){
	} finally {
	    try {inputStream.close();} catch (IOException ignored) {}
	}
	return data;
    }

    private static Gson getGson() {
	if(gson == null) {
	    GsonBuilder builder = new GsonBuilder();
	    builder.registerTypeAdapter(Inspiration.Data.class, new Inspiration.Data.DataAdapter().nullSafe());
	    builder.registerTypeAdapter(FoodInfo.Data.class, new FoodInfo.Data.DataAdapter().nullSafe());
	    builder.registerTypeAdapter(GobbleInfo.Data.class, new GobbleInfo.Data.DataAdapter().nullSafe());
	    builder.registerTypeAdapter(ArtificeData.class, new ArtificeData.DataAdapter().nullSafe());
	    builder.registerTypeAdapter(VariantsInfo.Data.class, new VariantsInfo.Data.DataAdapter().nullSafe());
	    builder.setPrettyPrinting();
	    gson =  builder.create();
	}
	return gson;
    }
}
