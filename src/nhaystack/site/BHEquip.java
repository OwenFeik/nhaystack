//
// Copyright (c) 2012, J2 Innovations
// Licensed under the Academic Free License version 3.0
//
// History:
//   06 Feb 2013  Mike Jarmy  Creation
//

package nhaystack.site;

import javax.baja.sys.*;

import haystack.*;
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
        }
    }
    -*/
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $nhaystack.site.BHEquip(3108637456)1.0$ @*/
/* Generated Fri Mar 29 12:39:07 EDT 2013 by Slot-o-Matic 2000 (c) Tridium, Inc. 2000 */

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
        String navName = Nav.makeNavFormat(this, tags);
        hdb.add("navName", navName);

        // dis
        String dis = createDis(server, tags, navName);
        hdb.add("dis", dis);

        // siteUri
        HUri siteUri = createSiteUri(server, tags, navName);
        if (siteUri != null) hdb.add("siteUri", siteUri);

        // add id
        HRef ref = NHRef.make(this).getHRef();
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
                String siteNavName = Nav.makeNavFormat(site, siteTags);

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
                String siteNavName = Nav.makeNavFormat(site, siteTags);

                return HUri.make("/site/" + siteNavName + "/" + navName);
            }
        }

        return null;
    }

    public BIcon getIcon() { return ICON; }

////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////

    private static final BIcon ICON = BIcon.make("module://nhaystack/nhaystack/icons/equip.png");

    private static final HDict ESSENTIALS;
    static
    {
        HDictBuilder hd = new HDictBuilder();
        hd.add("siteRef", HRef.make("null"));
        ESSENTIALS = hd.toDict();
    }
}

