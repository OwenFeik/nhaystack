//
// Copyright (c) 2012, J2 Innovations
// Licensed under the Academic Free License version 3.0
//
// History:
//   06 Feb 2013  Mike Jarmy  Creation
//

package nhaystack.site;

import javax.baja.sys.*;

import org.projecthaystack.*;
import nhaystack.*;
import nhaystack.server.*;

/**
 *  BHEquip represents a Haystack 'equip' rec.
 */
public class BHEquip extends BHTagged
{
    /*-
    class BHEquip
    {
        properties
        {
            haystack:  BHDict 
                default{[ BHDict.make("navNameFormat:\"%parent.displayName%\"") ]}
        }
    }
    -*/
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $nhaystack.site.BHEquip(1762613286)1.0$ @*/
/* Generated Tue Sep 03 09:48:31 EDT 2013 by Slot-o-Matic 2000 (c) Tridium, Inc. 2000 */

////////////////////////////////////////////////////////////////
// Property "haystack"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the <code>haystack</code> property.
   * @see nhaystack.site.BHEquip#getHaystack
   * @see nhaystack.site.BHEquip#setHaystack
   */
  public static final Property haystack = newProperty(0, BHDict.make("navNameFormat:\"%parent.displayName%\""),null);
  
  /**
   * Get the <code>haystack</code> property.
   * @see nhaystack.site.BHEquip#haystack
   */
  public BHDict getHaystack() { return (BHDict)get(haystack); }
  
  /**
   * Set the <code>haystack</code> property.
   * @see nhaystack.site.BHEquip#haystack
   */
  public void setHaystack(BHDict v) { set(haystack,v,null); }

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BHEquip.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

    /**
      * Return default values for those tags which are essential for
      * defining this component.
      */
    public HDict getDefaultEssentials()
    {
        return ESSENTIALS;
    }
  
    /**
      * Generate all the tags for this component, including
      * autogenerated tags like "id", and any other tags 
      * defined in the 'haystack' property.
      */
    public HDict generateTags(NHServer server)
    {
        HDictBuilder hdb = new HDictBuilder();

        // add annotated
        HDict tags = getHaystack().getDict();
        hdb.add(tags);

        // navName
        String navName = Nav.makeNavName(this, tags);
        hdb.add("navName", navName);

        // dis
        String dis = createDis(server, tags, navName);
        hdb.add("dis", dis);

//        // siteUri
//        HUri siteUri = createSiteUri(server, tags, navName);
//        if (siteUri != null) hdb.add("siteUri", siteUri);

        // add id
        HRef ref = server.makeComponentRef(this).getHRef();
        hdb.add("id", HRef.make(ref.val, dis));

        // add equip
        hdb.add("equip");

        // add misc other tags
        hdb.add("axType", getType().toString());
        hdb.add("axSlotPath", getSlotPath().toString());

        return hdb.toDict();
    }

    private String createDis(NHServer server, HDict tags, String navName)
    {
        String dis = navName;

        // site
        if (tags.has("siteRef"))
        {
            BComponent site = server.lookupComponent(tags.getRef("siteRef"));
            if (site != null)
            {
                HDict siteTags = BHDict.findTagAnnotation(site);
                String siteNavName = Nav.makeNavName(site, siteTags);

                dis = siteNavName + " " + navName;
            }
        }

        return dis;
    }

    private HUri createSiteUri(NHServer server, HDict tags, String navName)
    {
        // site
        if (tags.has("siteRef"))
        {
            BComponent site = server.lookupComponent(tags.getRef("siteRef"));
            if (site != null)
            {
                HDict siteTags = BHDict.findTagAnnotation(site);
                String siteNavName = Nav.makeNavName(site, siteTags);

                return HUri.make("sep:/" + siteNavName + "/" + navName);
            }
        }

        return null;
    }

    public BIcon getIcon() { return ICON; }

////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////

    public static final BIcon ICON = BIcon.make("module://nhaystack/nhaystack/icons/equip.png");

    private static final HDict ESSENTIALS;
    static
    {
        HDictBuilder hd = new HDictBuilder();
        hd.add("siteRef", HRef.make("null"));
        ESSENTIALS = hd.toDict();
    }
}

