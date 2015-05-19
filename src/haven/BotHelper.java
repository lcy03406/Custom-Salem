package haven;

import haven.Fightview.Relation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static haven.MCache.tilesz;

public class BotHelper {
    
    private static boolean aNull() {
	UI ui = UI.instance;
	if(ui == null || ui.gui == null || ui.gui.map == null || ui.gui.map.player()==null)
	    return true;
	return false;
    }

    private static boolean debugging = false;
    /**
     * 切换debug信息的显示
     */
    public static void debugging() {
	debugging = !debugging;
    }
    /**
     * 在System频道显示调试信息，受debugging()控制
     * @param str 字符串
     */
    public static void debug(String str) {
	if (debugging)
	    print(str);
    }

    /**
     * 在System频道显示信息
     * @param str 字符串
     */
    public static void print(String str) {
	if (aNull())
	    return;
	UI.instance.gui.syslog.append(str, Color.GREEN);
    }
    
    /**
     * 获取玩家自身
     * @return 玩家自身
     */
    public static Gob player() {
	if (aNull())
	    return null;
	return UI.instance.gui.map.player();
    }
    
    /**
     * 获取玩家自身的位置
     * @return 玩家自身位置
     */
    public static Coord pos() {
	if (aNull())
	    return null;
	return new Coord(UI.instance.gui.map.player().rc);
    }
    
    /**
     * 获取Soft Temper （体质条的第二个数）
     * @param i 0～3 红蓝黄黑
     * @return 整数，单位是千分之一
     */
    public static int stm(int i) {
	if (aNull())
	    return 0;
	return UI.instance.gui.tm.soft[i];
    }
    
    /**
     * 获取Hard Temper （体质条的第一个数）
     * @param i 0～3 红蓝黄黑
     * @return 整数，单位是千分之一
     */
    public static int htm(int i) {
	if (aNull())
	    return 0;
	return UI.instance.gui.tm.hard[i];
    }
    
    /**
     * 获取当前负重值
     * @return 整数，单位是千分之一
     */
    public static int weight() {
	if (aNull())
	    return 0;
	return UI.instance.gui.weight;
    }
    
    /**
     * 获取当前负重上限
     * @return 整数，单位是千分之一
     */
    public static int weightCap() {
	if (aNull())
	    return 0;
	Glob.CAttr ca = UI.instance.sess.glob.cattr.get("carry");
	if(ca == null)
	    return 0;
	return ca.comp;
    }
    
    /**
     * 获取当前手中的物品
     * @return 物品
     */
    public static GItem hand() {
	if (aNull())
	    return null;
	WItem w = UI.instance.gui.vhand;
	if (w == null)
	    return null;
	return w.item;
    }
    
    private static Random rander = new Random();
    private static Coord cc = new Coord(400,300);
    private static Coord cc() {
	return cc.add(rander.nextInt(400)-200, rander.nextInt(300)-150);
    }
    private static Coord ic() {
	return new Coord(rander.nextInt(30)+2, rander.nextInt(30)+2);
    }

    /**
     * 字符串转换为功能键值
     * @param str 字符串。'S','C','A'分别表示Shift, Ctrl, Alt
     * @return 整数。1=Shift, 2=Ctrl, 3=Alt, 可相加
     */
    public static int modflags(String str) {
	int flags = 0;
	if (str.contains("S"))
	    flags |= 1;
	if (str.contains("C"))
	    flags |= 2;
	if (str.contains("A"))
	    flags |= 4;
	return flags;
    }
    
    /**
     * 当前动作
     */
    public static String action;
    /**
     * 当前打开的容器
     */
    public static Inventory box;
    /**
     * 等待事件
     */
    private static String waiting;
    private static Object waiter = new Object();
    private static boolean waitsig = false;
    /**
     * 唤醒脚本。当然在脚本里是没用的。
     * @param w
     */
    public static void wake(String w) {
	debug("wake " + w);
	synchronized (waiter) {
	    if (w.equals(waiting)) {
		waitsig = true;
		waiter.notify();
	    }
	}
    }
    
    private static interface Do
    {
	public void doSomething();
    };
    private static void doAndWait(Do inner, String w, int timeout) throws InterruptedException {
	if (aNull())
	    return;
	synchronized (waiter) {
	    waiting = w;
	    waitsig = false;
	}
	inner.doSomething();
	synchronized (waiter) {
	    if (!waitsig) {
		waiter.wait(timeout);
	    }
	    waitsig = false;
	    waiting = "";
	}
    }
    
    /**
     * 等待一个事件
     * @param w 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void waitFor(String w, int timeout) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {}}, w, timeout);
    }
    
    private static String getCursForAct(String a) {
	switch (a) {
	case "pave":
	case "carry":
	    return "chi";
	case "destroy":
	case "repair":
	    return "kreuz";
	case "sift":
	    return "sft";
	case "dig":
	case "mine": return a;
	}
	return "";
    }
    
    /**
     * 执行动作并等待鼠标指针变化
     * @param a 动作名  mine dig sift carry pave repair destroy
     * 
     */
    public static void act(final String a) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    UI.instance.gui.act(a);
	    action = a;
	}}, "curs:gfx/hud/curs/" + getCursForAct(a), 5000);
    }
    
    /**
     * 选择菊花菜单选项，并等待事件
     * @param a 选项
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void flower(final String a, String waitfor, int timeout) throws InterruptedException {
	if (FlowerMenu.instance == null)
	    return;
	doAndWait( new Do(){public void doSomething() {
	    FlowerMenu.instance.chooseName(a);
	}}, waitfor, timeout);
    }
    
    /**
     * 获取地表
     * @param c 坐标
     * @return 地形ID
     *  铺石头13 铺gniess23 土 42 浅水59 深水61
     */
    public static int mapTile(Coord c) {
	//旧的 深水58，浅水57，铺炉渣11，铺砖17，铺石头18，土31，矿洞42，黑森林44，枫叶林7
	if (aNull())
	    return -1;
	return UI.instance.sess.glob.map.gettile(c.div(tilesz));
    }

    /**
     * 获取地表高度
     * @param c 坐标
     * @return 高度
     */
    public static float mapZ(Coord c) {
	if (aNull())
	    return -1;
	return UI.instance.sess.glob.map.getcz(c);
    }

    /**
     * 获取地表工具选中范围起点
     * @return 坐标
     */
    public static Coord mapToolC1() {
	if (aNull())
	    return null;
	FlatnessTool ft = FlatnessTool.instance(null);
	if (ft == null)
	    return null;
	return ft.c1.mul(tilesz);
    }
    
    /**
     * 获取地表工具选中范围终点
     * @return 坐标
     */
    public static Coord mapToolC2() {
	if (aNull())
	    return null;
	FlatnessTool ft = FlatnessTool.instance(null);
	if (ft == null)
	    return null;
	return ft.c2.mul(tilesz);
    }
    
    /**
     * 显示路径
     * @param path 路径，一串坐标
     */
    public static void mapShowPath(Iterable<Coord> path) {
	if (aNull())
	    return;
	MapView map = UI.instance.gui.map;
	map.setBotPath(path);
    }
    
    private static ArrayList<MCache.Overlay> ols = null;
    /**
     * 标出指定位置 （别用，巨卡）
     * @param points 一堆坐标
     */
    public static void mapShowPoints(Iterable<Coord> points) {
	if (aNull())
	    return;
	MCache mc = UI.instance.sess.glob.map;
	MapView mv = UI.instance.gui.map;
	if (ols == null) {
	    mv.enol(17);
	    ols = new ArrayList<MCache.Overlay>();
	}
	for (MCache.Overlay ol : ols) {
	    ol.destroy();
	}
	ols.clear();
	if (points == null)
	    return;
	for (Coord c : points) {
	    Coord tc = c.div(MCache.tilesz);
	    MCache.Overlay ol = mc.new Overlay(tc, tc, 1<<17);
	    ols.add(ol);
	}
    }
    
    /**
     * 点击地面，并等待事件
     * @param mc 坐标
     * @param button 按键。1=左，3=右
     * @param modflags 功能键
     * @param waitfor 事件
     * @param timeout 超时
     * 
     */
    public static void mapClick(final Coord mc, final int button, final int modflags, String waitfor, int timeout) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "click", cc(), mc, button, modflags);
	}}, waitfor, timeout);
    }
    
    /**
     * 移动，并等待移动停止
     * @param mc 坐标
     * 
     */
    public static void mapMove(Coord mc) throws InterruptedException {
	if (aNull())
	    return;
	mapClick(mc, 1, 0, "move_stop", (int)(pos().dist(mc)*200));
    }
    
    /**
     * 放置建筑
     * @param mc 坐标
     * @param angle 角度，度数，正东为0，正北为90
     * @param modflags 功能键
     * 
     */
    public static void mapPlace(final Coord mc, final int angle, int modflags) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "place", mc, angle, 1, 0);
	}}, "pack", (int)(pos().dist(mc)*120));
    }
    
    /**
     * 用于匹配名字的辅助函数。在gobFind和gobsArea函数内部用matchName匹配名字。
     * 匹配方式是：
     * 如果name为空，则返回true；
     * 如果name以"/"结尾，且gobname以name开始（使用String的startsWith方法），则返回true；
     * 如果gobname与name相等（使用String的equals方法），则返回true；
     * 否则返回false。
     */
    public static boolean matchName(String gobname, String name) {
	if (name.isEmpty())
	    return true;
	if (name.endsWith("/") && gobname.startsWith(name))
	    return true;
	return gobname.equals(name);
    }

    private static List<String> confForage = new ArrayList<String>();
    static {
	confForage.add("gfx/terobjs/herbs/");
	confForage.add("gfx/terobjs/items/");
	confForage.add("gfx/kritter/crab/");
	confForage.add("gfx/kritter/frog/");
    }
    
    /**
     * 设置Ctrl+A自动拾取的物体类型。
     * 默认是"gfx/terobjs/herbs/","gfx/terobjs/items/","gfx/kritter/crab/","gfx/kritter/frog/"
     * @param conf 物体名
     */
    public static void configForage(List<String> conf) {
	confForage = conf;
    }
    
    /**
     * 检查gobname与configForage的物体类型是否匹配
     * @param gobname 要匹配的物体名
     * @return 是否匹配
     */
    public static boolean matchForage(String gobname) {
	for (String name : confForage) {
	    if (matchName(gobname, name)) {
		return true;
	    }
	}
	return false;
    }
    
    
    private static List<String> confChoose = new ArrayList<String>();
    static {
	confChoose.add("Pick");
	confChoose.add("Pry Face");
	confChoose.add("Smash Face");
	confChoose.add("She Loves Me");
	confChoose.add("She Loves Me Not");
	confChoose.add("Remove Cone Scales");
    }
    
    /**
     * 设置自动选择的菊花菜单选项。
     * 默认是"Pick","Pry Face","Smash Face","She Loves Me","She Loves Me Not","Remove Cone Scales"
     * @param conf 选项名
     */
    public static void configChoose(List<String> conf) {
	confChoose = conf;
    }

    /**
     * 检查petalname与configChoose是否匹配
     * @param petalname 要检查的选项
     * @return 是否匹配
     */
    public static boolean matchChoose(String petalname) {
	for (String name : confChoose) {
	    if (petalname.equals(name)) {
		return true;
	    }
	}
	return false;
    }
    
    /**
     * 搜索最近的某种物体
     * @param name 物体名字（可以开debuging然后点击物品看到）@see matchName
     * @param center 搜索中心点
     * @param radius 最大搜索半径
     * @return 物体
     * 
     */
    public static Gob gobFind(String name, Coord center, double radius) {
	if (aNull())
	    return null;
	double r = radius;
	Gob g = null;
	OCache oc = UI.instance.gui.map.glob.oc;
	synchronized (oc) {
	    for (Gob gob : oc){
		if (gob == player())
		    continue;
                if(matchName(gob.name(), name)) {
                    double d = gob.rc.dist(center);
                    if (d < r) {
                	g = gob;
                	r = d;
                    }
                }
	    }
	}
	return g;
    }
    
    /**
     * 搜索矩形区域内的所有物体
     * @param name 物体名字（可以开debuging然后点击物品看到）@see matchName
     * @param min 最小坐标XY
     * @param max 最大坐标XY
     * @return 一堆物体
     */
    public static ArrayList<Gob> gobsArea(String name, Coord min, Coord max) {
	if (aNull())
	    return null;
	OCache oc = UI.instance.gui.map.glob.oc;
	ArrayList<Gob> a = new ArrayList<Gob>();
	synchronized (oc) {
	    for (Gob gob : oc){
		if (matchName(gob.name(), name)) {
                    if (gob.rc.in(min, max)) {
                	a.add(gob);
                    }
                }
	    }
	}
	return a;
    }
    
    /**
     * 点击物体，并等待事件
     * @param gob 物体
     * @param button 按键。1=左，3=右
     * @param modflags 功能键
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void gobClick(final Gob gob, final int button, final int modflags, String waitfor, int timeout) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "click", gob.sc, gob.rc, button, modflags, 0, (int)gob.id, gob.rc, 0, MapView.getid(gob));
	}}, waitfor, timeout);
    }
    /**
     * 点击物体的指定部位，并等待事件
     * @param gob 物体
     * @param part 部位。风箱末端是左17、右18、上19。
     * @param button 按键。1=左，3=右
     * @param modflags 功能键
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void gobClickPart(final Gob gob, final int part, final int button, final int modflags, String waitfor, int timeout) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "click", gob.sc, gob.rc, button, modflags, 0, (int)gob.id, gob.rc, 0, part);
	}}, waitfor, timeout);
    }    
    /**
     * 手拿物品与物体交互，并等待事件
     * @param gob 物体
     * @param modflags 功能键
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void gobItemact(final Gob gob, final int modflags, String waitfor, int timeout) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "itemact", gob.sc, gob.rc, modflags, (int)gob.id, gob.rc, MapView.getid(gob));
	}}, waitfor, timeout);
    }
    
    /**
     * 半自动打怪/扫野。快捷键Ctrl+A
     * 如果在战斗中，则跟随战斗目标；
     * 否则搜索最近的herbs、items、crab、frog，右键。
     */
    public static void forage() {
	if (aNull())
	    return;
	double r = 400;
	OCache oc = UI.instance.gui.map.glob.oc;
	Fightview fv = UI.instance.gui.fv;
        if(fv != null && !fv.lsrel.isEmpty()){
            Relation rel = fv.lsrel.getFirst();
            Gob enemy = oc.getgob(rel.gobid);
            if(enemy!=null && !enemy.virtual)
            {
        	try {
        	    BotHelper.gobClick(enemy, 1, 0, "", 1);
        	} catch (Exception e) {
        	}
        	return;
            }
        }
	Gob g = null;
	Coord pos = pos();
	Moving m = player().getattr(Moving.class);
        if(m!=null) {
            if(m instanceof LinMove) {
                LinMove lm = (LinMove) m;
                pos = lm.t;
            }	    
	}
	synchronized (oc) {
	    for (Gob gob : oc){
		String gobname = gob.name();
		if (matchForage(gobname)) {
                    double d = gob.rc.dist(pos);
                    if (d < r) {
                	g = gob;
                	r = d;
                    }
                }
	    }
	}
	if (g != null) {
	    try {
		BotHelper.gobClick(g, 3, 0, "", 1);
	    } catch (Exception e) {
	    }
	}
    }

    /**
     * 自己的物品栏
     * @return 物品栏
     */
    public static Inventory inv() {
	if (aNull())
	    return null;
	return UI.instance.gui.maininv;
    }
    
    /**
     * 建筑的物品栏
     * @return 物品栏
     */
    public static Inventory box() {
	if (aNull())
	    return null;
	return box;
    }
   
    /**
     * 查找建筑里的一个物品
     * @param name 物品名字
     * @return 物品
     */
    public static GItem itemBoxFind(String name) {
	Inventory inv = box;
	if (inv == null)
	    return null;
	return inv.findItem(name);
    }
    
    /**
     * 查找建筑里的所有物品
     * @param name 物品名字
     * @return 物品
     */
    public static ArrayList<GItem> itemBoxAll(String name) {
	Inventory inv = box;
	if (inv == null)
	    return null;
	return inv.allItem(name);
    }
    
    /**
     * 查找建筑里的空格
     * @return 空格坐标，用于itemBoxDrop()。如建筑已满则返回null。
     */
    public static Coord itemBoxEmpty() {
	if (box == null)
	    return null;
	return box.findEmpty();
    }
    /**
     * 手中物品放进建筑
     * 
     */
    public static void itemBoxDrop(final Coord c) throws InterruptedException {
	final Inventory inv = box;
	if (inv == null)
	    return;
	if (hand() == null)
	    return;
	doAndWait( new Do(){public void doSomething() {
	    inv.wdgmsg("drop", c);
	}}, "hand_drop", 5000);
    }    
    /**
     * 查找物品栏里的一个物品
     * @param name 物品名字
     * @return 物品
     */
    public static GItem itemFind(String name) {
	Inventory inv = inv();
	if (inv == null)
	    return null;
	return inv.findItem(name);
    }
    
    /**
     * 查找物品栏里的所有物品
     * @param name 物品名字
     * @return 物品
     */
    public static ArrayList<GItem> itemAll(String name) {
	Inventory inv = inv();
	if (inv == null)
	    return null;
	return inv.allItem(name);
    }    

    /**
     * 手中物品放进物品栏
     * 
     */
    public static void itemDrop() throws InterruptedException {
	final Inventory inv = inv();
	if (inv == null)
	    return;
	if (hand() == null)
	    return;
	doAndWait( new Do(){public void doSomething() {
	    inv.wdgmsg("drop", inv.findEmpty());
	}}, "hand_drop", 5000);
    }    

    /**
     * 拿起物品
     * @param item 物品
     * 
     */
    public static void itemTake(final GItem item) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    item.wdgmsg("take", ic());
	}}, "hand_take", 5000);
    }
    
    /**
     * 转移物品，从物品栏到建筑或者从建筑到物品栏。注意：运行脚本时不要打开背包！
     * @param item 物品
     * 
     */
    public static void itemTransfer(final GItem item) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    item.wdgmsg("transfer", ic());
	}}, "", 100);
    } 
    
    /**
     * 右键物品，并等待事件
     * @param item 物品
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void itemAct(final GItem item, String waitfor, int timeout) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    item.wdgmsg("iact", ic());
	}}, waitfor, timeout);
    }
    
    /**
     * 手拿物品与物品交互，并等待事件
     * @param item 物品
     * @param modflags 功能键
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void itemItemact(final GItem item, final int modflags, String waitfor, int timeout) throws InterruptedException {
	doAndWait( new Do(){public void doSomething() {
	    item.wdgmsg("itemact", modflags);
	}}, waitfor, timeout);
    }
    
    /**
     * 看花盆是否有水
     * @param pot 必须是一个花盆
     * @return 是否
     */
    public static boolean potWater(Gob pot) {
	int mask = pot.mask();
	return (mask & 2) != 0;
    }
    /**
     * 看花盆是否有屎
     * @param pot 必须是一个花盆
     * @return 是否
     */
    public static boolean potHumus(Gob pot) {
	int mask = pot.mask();
	return (mask & 1) != 0;
    }

    /**
     * 看花盆有几个植物
     * @param pot 必须是一个花盆
     * @return 几个
     */
    public static int potPlants(Gob pot) {
	return pot.ols.size();
    }
}
