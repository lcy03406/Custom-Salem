package haven;

import java.util.HashMap;
import java.util.Map;







import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.transform.ThreadInterrupt;

import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;

public class BotCommander implements Console.Directory {
    public static final BotCommander instance = new BotCommander();
    private GroovyScriptEngine gse;
    private Binding binding;
    private ThreadGroup tg;
    private BotThread worker;
    private Map<String, Console.Command> cmds = new HashMap<String, Console.Command>();

    BotCommander() {
	String folder = "scripts/";
	try {
            gse = new GroovyScriptEngine(folder);
            ImportCustomizer imp = new ImportCustomizer();
            imp.addStaticStars("haven.BotHelper");
            imp.addStarImports("haven");
            gse.getConfig().addCompilationCustomizers(imp);
            ASTTransformationCustomizer ast = new ASTTransformationCustomizer(ThreadInterrupt.class);
            gse.getConfig().addCompilationCustomizers(ast);
            binding = new Binding();
            tg = new ThreadGroup("bots");
        } catch (Exception e) {
            return;
        }
	cmds.put("north", new Console.Command() {
	    @Override
	    public void run(Console cons, String[] args) throws Exception {
		Coord c =  BotHelper.pos();
		c.y += 11;
		BotHelper.mapMove(c);
	    }});
	cmds.put("debugbot", new Console.Command() {
	    @Override
	    public void run(Console cons, String[] args) throws Exception {
		BotHelper.debugging();
	    }});
	cmds.put("bot", new Console.Command() {
	    @Override
	    public void run(Console cons, String[] args) throws Exception {
		if (args.length < 2)
		    return;
		bot(args[1]);
	    }});
	cmds.put("killbot", new Console.Command() {
	    @Override
	    public void run(Console cons, String[] args) throws Exception {
		killbot();
	    }});
    }

    @Override
    public Map<String, Console.Command> findcmds() {
	return cmds;
    }
    private void bot (String name) {
	if (worker != null) {
	    BotHelper.print("ERROR! A bot is already running!");
	    return;
	}
	worker = new BotThread(name);
	worker.start();
    }
    private void killbot () {
	Thread t = worker;
	if (t != null) {
	    t.interrupt();
	}
    }
    
    private class BotThread extends Thread {
	private String name;
	BotThread(String name) {
	    super(tg, "Bot " + name);
	    this.name = name;
	}
	@Override
	public void run() {
	    try {
		sleep(0); //to ease the warning that InterruptedException is never thrown.
		BotHelper.print("BOT INFO: " + name + " is working.");
		gse.run(name + ".groovy", binding);
		BotHelper.print("BOT INFO: Job Done.");
	    } catch (InterruptedException e) {
		BotHelper.print("BOT INFO: " + name + " has been killed, aah!");
	    } catch (Exception e) {
		BotHelper.print("BOT ERROR! " + e.getMessage());
	    } finally {
		worker = null;
	    }
	}
    }

}
