package haven;

import java.awt.Color;
import java.util.ArrayList;
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
    public static Inventory box;
    /**
     * 等待事件
     */
    public static String waiting;
    public static Object waiter = new Object();
    /**
     * 唤醒脚本。当然在脚本里是没用的。
     * @param w
     */
    public static void wake(String w) {
	synchronized (waiter) {
	    debug("wake " + w);
	    if (w.equals(waiting))
		waiter.notify();
	}
    }
    
    /**
     * 等待一个事件
     * @param w 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void waitFor(String w, int timeout) throws InterruptedException {
	synchronized (waiter) {
	    waiting = w;
	    waiter.wait(timeout);
	}
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
    public static void act(String a) throws InterruptedException {
	if (aNull())
	    return;
	synchronized (waiter) {
	    waiting = "curs:gfx/hud/curs/" + getCursForAct(a); 
	    UI.instance.gui.act(a);
	    waiter.wait(5000);
	    waiting = "";
	    action = a;
	}
    }
    
    /**
     * 选择菊花菜单选项，并等待事件
     * @param a 选项
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void flower(String a, String waitfor, int timeout) throws InterruptedException {
	synchronized (waiter) {
	    waiting = waitfor; 
	    if (FlowerMenu.instance != null) {
		FlowerMenu.instance.chooseName(a);
	    }
	    waiter.wait(timeout);
	    waiting = "";
	}
    }
    
    /**
     * 获取地表
     * @param c 坐标
     * @return 地形ID：深水58，浅水57，铺地18，矿洞42，黑森林44，枫叶林7
     */
    public static int mapTile(Coord c) {
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
    public static void mapClick(Coord mc, int button, int modflags, String waitfor, int timeout) throws InterruptedException {
	if (aNull())
	    return;
	synchronized (waiter) {
	    waiting = waitfor; 
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "click", cc(), mc, button, modflags);
	    waiter.wait(timeout);
	    waiting = "";
	}
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
    public static void mapPlace(Coord mc, int angle, int modflags) throws InterruptedException {
	if (aNull())
	    return;
	synchronized (waiter) {
	    waiting = "pack"; 
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "place", mc, angle, 1, 0);
	    waiter.wait((int)(pos().dist(mc)*120));
	    waiting = "";
	}
    }
    
    /**
     * 搜索最近的某种物体
     * @param name 物体名字（可以开debuging然后点击物品看到）（空字符串表示不判断物体名字）
     * @param center 搜索中心点
     * @param radius 最大搜索半径
     * @return 物体
     */
    public static Gob gobFind(String name, Coord center, double radius) {
	if (aNull())
	    return null;
	double r = radius;
	Gob g = null;
	OCache oc = UI.instance.gui.map.glob.oc;
	synchronized (oc) {
	    for (Gob gob : oc){
                if(gob.name().equals(name) || name.isEmpty()) {
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
     * @param name 物体名字（可以开debuging然后点击物品看到）（空字符串表示不判断物体名字）
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
                if(gob.name().equals(name) || name.isEmpty()) {
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
    public static void gobClick(Gob gob, int button, int modflags, String waitfor, int timeout) throws InterruptedException {
	if (aNull())
	    return;
	synchronized (waiter) {
	    waiting = waitfor; 
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "click", gob.sc, gob.rc, button, modflags, 0, (int)gob.id, gob.rc, 0, MapView.getid(gob));
	    waiter.wait(timeout);
	    waiting = "";
	}
    }
    /**
     * 点击物体的指定部位，并等待事件
     * @param gob 物体
     * @param part 部位。风箱末端是16。
     * @param button 按键。1=左，3=右
     * @param modflags 功能键
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void gobClickPart(Gob gob, int part, int button, int modflags, String waitfor, int timeout) throws InterruptedException {
	if (aNull())
	    return;
	synchronized (waiter) {
	    waiting = waitfor; 
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "click", gob.sc, gob.rc, button, modflags, 0, (int)gob.id, gob.rc, 0, part);
	    waiter.wait(timeout);
	    waiting = "";
	}
    }    
    /**
     * 手拿物品与物体交互，并等待事件
     * @param gob 物体
     * @param modflags 功能键
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void gobItemact(Gob gob, int modflags, String waitfor, int timeout) throws InterruptedException {
	if (aNull())
	    return;
	synchronized (waiter) {
	    waiting = waitfor; 
	    MapView map = UI.instance.gui.map;
	    map.wdgmsg(map, "itemact", gob.sc, gob.rc, modflags, (int)gob.id, gob.rc, MapView.getid(gob));
	    waiter.wait(timeout);
	    waiting = "";
	}
    }
    
    private static Inventory inv() {
	if (aNull())
	    return null;
	return UI.instance.gui.maininv;
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
    public static void itemBoxDrop(Coord c) throws InterruptedException {
	Inventory inv = box;
	if (inv == null)
	    return;
	if (hand() == null)
	    return;
	synchronized (waiter) {
	    waiting = "hand_drop";
	    inv.wdgmsg("drop", c);
	    waiter.wait(5000);
	    waiting = "";
	    waiter.wait(500);
	}
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
	Inventory inv = inv();
	if (inv == null)
	    return;
	if (hand() == null)
	    return;
	synchronized (waiter) {
	    waiting = "hand_drop";
	    inv.wdgmsg("drop", inv.findEmpty());
	    waiter.wait(5000);
	    waiting = "";
	    waiter.wait(500);
	}
    }    

    /**
     * 拿起物品
     * @param item 物品
     * 
     */
    public static void itemTake(GItem item) throws InterruptedException {
	synchronized (waiter) {
	    waiting = "hand_take"; 
	    item.wdgmsg("take", ic());
	    waiter.wait(5000);
	    waiting = "";
	    waiter.wait(500);
	}
    }
    
    /**
     * 转移物品，从物品栏到建筑或者从建筑到物品栏。注意：运行脚本时不要打开背包！
     * @param item 物品
     * 
     */
    public static void itemTransfer(GItem item) throws InterruptedException {
	synchronized (waiter) {
	    waiting = ""; 
	    item.wdgmsg("transfer", ic());
	    waiter.wait(2000);
	}
    } 
    
    /**
     * 右键物品，并等待事件
     * @param item 物品
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void itemAct(GItem item, String waitfor, int timeout) throws InterruptedException {
	synchronized (waiter) {
	    waiting = waitfor; 
	    item.wdgmsg("iact", ic());
	    waiter.wait(timeout);
	    waiting = "";
	}
    }
    
    /**
     * 手拿物品与物品交互，并等待事件
     * @param item 物品
     * @param modflags 功能键
     * @param waitfor 事件
     * @param timeout 超时（毫秒）
     * 
     */
    public static void itemItemact(GItem item, int modflags, String waitfor, int timeout) throws InterruptedException {
	if (aNull())
	    return;
	synchronized (waiter) {
	    waiting = waitfor; 
	    item.wdgmsg("itemact", modflags);
	    waiter.wait(timeout);
	    waiting = "";
	}
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
	return (mask & 2) != 0;
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
