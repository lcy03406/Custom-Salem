package haven.res.lib.tree;

import haven.Config;
import haven.Location;
import haven.Matrix4f;
import haven.Message;
import haven.RenderList;
import haven.Resource;
import haven.Sprite;
import haven.States;
import haven.StaticSprite;

public class TreeSprite extends StaticSprite
{
  public final float fscale;

  public TreeSprite(Sprite.Owner paramOwner, Resource paramResource, float paramFloat)
  {
    super(paramOwner, paramResource, new Message(0));
    if(!Config.raidermodetrees)
    {
        this.fscale = paramFloat;
        if (paramFloat == 1.0F)
          this.scale = null;
        else
          this.scale = mkscale(paramFloat);
    }        
    else
    {
        this.fscale = 0.5f;
        this.scale = scale5;
    }
  }

  public TreeSprite(Sprite.Owner paramOwner, Resource paramResource, Message paramMessage) {
    super(paramOwner, paramResource, new Message(0));
    int i;
    if (paramMessage.eom())
      i = 100;
    else
      i = paramMessage.uint8();
    if(!Config.raidermodetrees)
    {
        this.fscale = (i / 100.0F);
        if (i == 100)
          this.scale = null;
        else
          this.scale = mkscale(this.fscale);
    }        
    else
    {
        this.fscale = 0.5f;
        this.scale = scale5;
    }
  }

  public boolean setup(RenderList paramRenderList) {
    return super.setup(paramRenderList);
  }
}