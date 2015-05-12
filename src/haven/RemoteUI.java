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

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import haven.Fightview.Relation;

public class RemoteUI implements UI.Receiver, UI.Runner {
    Session sess, ret;
    UI ui;
	
    public RemoteUI(Session sess) {
	this.sess = sess;
	Widget.initnames();
    }
	
    public void rcvmsg(int id, String name, Object... args) {
	String str = String.format("[S]%d:%s:", id, name);
	for(Object o : args) {
	    str += "&";
	    str += o.toString();
	}
	BotHelper.debug(str);
        
	Message msg = new Message(Message.RMSG_WDGMSG);
	msg.adduint16(id);
	msg.addstring(name);
	msg.addlist(args);
	sess.queuemsg(msg);
    }
	
    public void ret(Session sess) {
	synchronized(this.sess) {
	    this.ret = sess;
	    this.sess.notifyAll();
	}
    }
    
    static Set<String> msgLogIgnore = new HashSet<String>();
    static Set<String> msgLogDetail = new HashSet<String>();
    {
	msgLogIgnore.add("weight");
	msgLogIgnore.add("tmexp");
	msgLogIgnore.add("upd");
	msgLogIgnore.add("curs");
	msgLogIgnore.add("prog");
	msgLogIgnore.add("stm");
	msgLogIgnore.add("htm");
	msgLogIgnore.add("tt");
	msgLogDetail.add("pack");
    }

    public Session run(UI ui) throws InterruptedException {
	this.ui = ui;
	ui.setreceiver(this);
	while(true) {
	    Message msg;
	    while((msg = sess.getuimsg()) != null) {
		if(msg.type == Message.RMSG_NEWWDG) {
		    int id = msg.uint16();
		    String type = msg.string();
		    int parent = msg.uint16();
		    Object[] pargs = msg.list();
		    Object[] cargs = msg.list();
		    ui.newwidget(id, type, parent, pargs, cargs);
		    
//		    String str = String.format("[R.N]%d:%s:%d:", id, type, parent);
//		    for(Object o : pargs) {
//			str += "&";
//			str += o.toString();
//		    }
//		    str += ":";
//		    for(Object o : cargs) {
//			str += "&";
//			str += o.toString();
//		    }
//		    Helper.debug(str);
//		    System.out.println(str);

		} else if(msg.type == Message.RMSG_WDGMSG) {
		    int id = msg.uint16();
		    String name = msg.string();
		    Object[] args = msg.list();
		    ui.uimsg(id, name, args);
                    
		    checkvents(name, args);

		    if (!msgLogIgnore.contains(name))
		    {
			boolean detail = false;
			if (msgLogDetail.contains(name))
			{
			    detail = true;
			}
			
			String str = String.format("[R.M]%d:%s:", id, name);
			if (detail)
			{
			    for(Object o : args) {
				str += "&";
				str += o.toString();
			    }
			}
			BotHelper.debug(str);
		    }
		    
		    if (name.equals("curs")) {
			if (args.length == 0) {
			    BotHelper.wake("curs:null");
			} else {
			    String cur = (String) args[0];
			    BotHelper.wake("curs:" + cur);
			}
		    } else if (name.equals("prog") && args.length == 0) {
			BotHelper.wake("prog");
		    } else if (name.equals("err")) {
			BotHelper.wake("err");
		    }
		} else if(msg.type == Message.RMSG_DSTWDG) {
		    int id = msg.uint16();
		    ui.destroy(id);
                    
		    //String str = String.format("[R.D]%d", id);
		    //Helper.debug(str);
		    //System.out.println(str);
		}
	    }
	    synchronized(sess) {
		if(ret != null) {
		    sess.close();
		    return(ret);
		}
		if(!sess.alive())
		    return(null);
		sess.wait(50);
	    }
	}
    }

    private void checkvents(String name, Object[] args) {
	if(name.equals("prog")){
	    if(args.length == 0){
		progressComplete();
	    }
	}
    }

    private void progressComplete() {
	try {
	    if (Config.autosift && UI.isCursor(UI.Cursor.SIFTING)) {
		MapView map = UI.instance.gui.map;
		Gob player = map.player();
		map.wdgmsg(map, "click", player.sc, player.rc, 1, 0);
	    }
	} catch(Exception ignored){}
    }
}
