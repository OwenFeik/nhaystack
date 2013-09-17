//
// Copyright (c) 2012, J2 Innovations
// Licensed under the Academic Free License version 3.0
//
// History:
//   04 Oct 2012  Mike Jarmy  Creation
//
package nhaystack.server.storehouse;

import java.util.*;

import javax.baja.control.*;
import javax.baja.control.ext.*;
import javax.baja.driver.*;
import javax.baja.driver.point.*;
import javax.baja.history.*;
import javax.baja.history.ext.*;
import javax.baja.naming.*;
import javax.baja.status.*;
import javax.baja.sys.*;
import javax.baja.util.*;

import org.projecthaystack.*;
import nhaystack.*;
import nhaystack.collection.*;
import nhaystack.res.*;
import nhaystack.server.*;
import nhaystack.site.*;

/**
  * ComponentStorehouse manages access to the BComponentSpace
  */
public class ComponentStorehouse extends Storehouse
{
    public ComponentStorehouse(NHServer server)
    {
        super(server);
    }

    /**
      * Create the haystack representation of a BComponent.
      *
      * The haystack representation is a combination of the 
      * autogenerated tags, and those tags specified
      * in the explicit haystack annotation (if any).
      *
      * This method never returns null.
      */
    public HDict createComponentTags(BComponent comp)
    {
        HDictBuilder hdb = new HDictBuilder();

        if (comp instanceof BHTagged)
        {
            hdb.add(((BHTagged) comp).generateTags(server));
        }
        else
        {
            // add existing tags
            HDict tags = BHDict.findTagAnnotation(comp);
            if (tags == null) 
                tags = HDict.EMPTY;
            hdb.add(tags);

            // navName
            String navName = Nav.makeNavName(comp, tags);
            hdb.add("navName", navName);

            // add misc other tags
            hdb.add("axType", comp.getType().toString());
            hdb.add("axSlotPath", comp.getSlotPath().toString());

            // points get special treatment
            if (comp instanceof BControlPoint)
                createPointTags((BControlPoint) comp, hdb, tags);

            // dis
            String dis = createDis(hdb, navName);
            hdb.add("dis", dis);

//            // uri
//            HUri siteUri = createSiteUri(hdb, navName);
//            if (siteUri != null) hdb.add("siteUri", siteUri);

            // add id
            HRef ref = server.makeComponentRef(comp).getHRef();
            hdb.add("id", HRef.make(ref.val, dis));
        }

        // add custom tags
        hdb.add(server.createCustomTags(comp));

        // done
        return hdb.toDict();
    }

    private String createDis(HDictBuilder tags, String navName)
    {
        String dis = navName;

        if (tags.has("point"))
        {
            String equipNav = getRefNav(tags, "equipRef");
            if (equipNav != null)
            {
                dis = equipNav + " " + navName;

                String siteNav = getRefNav(tags, "siteRef");
                if (siteNav != null)
                    dis = siteNav + " " + equipNav + " " + navName;
            }
        }
        else if (tags.has("equip"))
        {
            String siteNav = getRefNav(tags, "siteRef");
            if (siteNav != null)
                dis = siteNav + " " + navName;
        }

        return dis;
    }

    private HUri createSiteUri(HDictBuilder tags, String navName)
    {
        if (tags.has("point"))
        {
            String equipNav = getRefNav(tags, "equipRef");
            if (equipNav != null)
            {
                String siteNav = getRefNav(tags, "siteRef");
                if (siteNav != null)
                    return HUri.make("sep:/" + siteNav + "/" + equipNav + "/" + navName);
            }
        }
        else if (tags.has("equip"))
        {
            String siteNav = getRefNav(tags, "siteRef");
            if (siteNav != null)
                return HUri.make("sep:/" + siteNav + "/" + navName);
        }

        return null;
    }

    private String getRefNav(HDictBuilder tags, String tagName)
    {
        if (tags.has(tagName))
        {
            BComponent comp = server.lookupComponent((HRef) tags.get(tagName));
            if (comp != null)
            {
                HDict compTags = BHDict.findTagAnnotation(comp);
                return Nav.makeNavName(comp, compTags);
            }
        }
        return null;
    }

    private void createPointTags(
        BControlPoint point, 
        HDictBuilder hdb,
        HDict tags)
    {
        // ensure there is a point marker tag
        hdb.add("point");

        // check if this point has a history
        BHistoryConfig cfg = server.getHistoryStorehouse()
            .lookupHistoryFromPoint(point);
        if (cfg != null)
        {
            hdb.add("his");

            if (service.getShowLinkedHistories())
                hdb.add("axHistoryRef", server.makeComponentRef(cfg).getHRef());

            // tz
            if (!tags.has("tz"))
            {
                HTimeZone tz = server.makeTimeZone(cfg.getTimeZone());
                if (tz != null) hdb.add("tz", tz.name);
            }

            // hisInterpolate 
            if (!tags.has("hisInterpolate"))
            {
                BHistoryExt historyExt = lookupHistoryExt(point);
                if (historyExt != null && (historyExt instanceof BCovHistoryExt))
                    hdb.add("hisInterpolate", "cov");
            }
        }

        // point kind tags
        int pointKind = getControlPointKind(point);
        BFacets facets = (BFacets) point.get("facets");
        addPointKindTags(pointKind, facets, tags, hdb);

        // cur, writable
        hdb.add("cur");
        if (point.isWritablePoint())
            hdb.add("writable");

        // curVal, curStatus
        switch(pointKind)
        {
            case NUMERIC_KIND:
                BNumericPoint np = (BNumericPoint) point;

                HNum curVal = null;
                if (tags.has("unit"))
                {
                    HVal unit = tags.get("unit");
                    curVal = HNum.make(np.getNumeric(), unit.toString());
                }
                else
                {
                    Unit unit = findUnit(facets);
                    if (unit == null) 
                        curVal = HNum.make(np.getNumeric());
                    else
                        curVal = HNum.make(np.getNumeric(), unit.symbol);
                }
                hdb.add("curVal", curVal);
                hdb.add("curStatus", makeStatusString(point.getStatus()));

                break;

            case BOOLEAN_KIND:
                BBooleanPoint bp = (BBooleanPoint) point;
                hdb.add("curVal",    HBool.make(bp.getBoolean()));
                hdb.add("curStatus", makeStatusString(point.getStatus()));
                break;

            case ENUM_KIND:
                BEnumPoint ep = (BEnumPoint) point;
                hdb.add("curVal",    HStr.make(ep.getEnum().toString()));
                hdb.add("curStatus", makeStatusString(point.getStatus()));
                break;

            case STRING_KIND:
                BStringPoint sp = (BStringPoint) point;
                hdb.add("curVal",    HStr.make(sp.getOut().getValue().toString()));
                hdb.add("curStatus", makeStatusString(point.getStatus()));
                break;
        }

        // actions
        switch(pointKind)
        {
            case NUMERIC_KIND:
            case ENUM_KIND:
            case STRING_KIND:
                hdb.add("actions", HStr.make(
                    "ver:\"2.0\"\n" + 
                    "dis,expr\n" + 
                    "\"Manual Set\",\"pointOverride(\\$self, \\$val, \\$duration)\"\n" + 
                    "\"Manual Auto\",\"pointAuto(\\$self)\"\n" + 
                    "\"Emergency Set\",\"pointEmergencyOverride(\\$self, \\$val)\"\n" + 
                    "\"Emergency Auto\",\"pointEmergencyAuto(\\$self)\""
                ));

                break;

            case BOOLEAN_KIND:
                hdb.add("actions", HStr.make(
                    "ver:\"2.0\"\n" + 
                    "dis,expr\n" + 
                    "\"Manual On\",\"pointOverride(\\$self, true, \\$duration)\"\n" + 
                    "\"Manual Off\",\"pointOverride(\\$self, false, \\$duration)\"\n" + 
                    "\"Manual Auto\",\"pointAuto(\\$self)\""
                ));

                break;
        }

        // the point is explicitly tagged with an equipRef
        if (tags.has("equipRef"))
        {
            BComponent equip = server.lookupComponent((HRef) tags.get("equipRef"));

            // try to look up  siteRef too
            HDict equipTags = BHDict.findTagAnnotation(equip);
            if (equipTags.has("siteRef"))
                hdb.add("siteRef", equipTags.get("siteRef"));
        }
        // maybe we've cached an implicit equipRef
        else
        {
            BComponent equip = server.getCache().getImplicitEquip(point);
            if (equip != null)
            {
                hdb.add("equipRef", server.makeComponentRef(equip).getHRef());

                // try to look up  siteRef too
                HDict equipTags = BHDict.findTagAnnotation(equip);
                if (equipTags.has("siteRef"))
                    hdb.add("siteRef", equipTags.get("siteRef"));
            }
        }
    }

    /**
      * Return whether the given component
      * ought to be turned into a Haystack record.
      */
    public boolean isVisibleComponent(BComponent comp)
    {
        // Return true for components that have been 
        // annotated with a BHDict instance.
        if (comp instanceof BHTagged) 
            return true;

        // Return true for BControlPoints.
        if (comp instanceof BControlPoint)
            return true;

        // Return true for components that are annotated with a BHDict.
        BValue haystack = comp.get("haystack");
        if ((haystack != null) && (haystack instanceof BHDict))
            return true;

        // nope
        return false;
    }

    /**
      * Iterate through all the points
      */
    public Iterator makeIterator()
    {
        return new CIterator();
    }

    /**
      * Try to find the point that goes with a history,
      * or return null.
      */
    public BControlPoint lookupPointFromHistory(BHistoryConfig cfg)
    {
        // local history
        if (cfg.getId().getDeviceName().equals(Sys.getStation().getStationName()))
        {
            try
            {
                BOrd[] ords = cfg.getSource().toArray();
                if (ords.length == 1) 
                {
                    BComponent source = (BComponent) ords[0].resolve(service, null).get();

                    // The source is not always a BHistoryExt.  E.g. for 
                    // LogHistory its the LogHistoryService.
                    if (source instanceof BHistoryExt)
                    {
                        if (source.getParent() instanceof BControlPoint)
                            return (BControlPoint) source.getParent();
                    }
                }
            }
            catch (UnresolvedException e)
            {
                return null;
            }

            return null;
        }
        // look for imported point that goes with history (if any)
        else
        {
            RemotePoint remote = RemotePoint.fromHistoryConfig(cfg);
            if (remote == null) return null;

            return server.getCache().getControlPoint(remote);
        }
    }

////////////////////////////////////////////////////////////////
// private
////////////////////////////////////////////////////////////////

    /**
      * Find the imported point that goes with an imported history, 
      * or return null.  
      */
    private BControlPoint lookupRemotePoint(
        BHistoryConfig cfg, 
        RemotePoint remote)
    {
        // look up the station
        BDeviceNetwork network = service.getNiagaraNetwork();
        BDevice station = (BDevice) network.get(remote.getStationName());
        if (station == null) return null;

        // look up the points
        // this fetches from sub-folders too
        BPointDeviceExt pointDevExt = (BPointDeviceExt) station.get("points");
        BControlPoint[] points = pointDevExt.getPoints(); 

        // find a point with matching slot path
        for (int i = 0; i < points.length; i++)
        {
            BControlPoint point = points[i];

            // Check for a NiagaraProxyExt
            BAbstractProxyExt proxyExt = point.getProxyExt();
            if (!proxyExt.getType().is(RemotePoint.NIAGARA_PROXY_EXT)) continue;

            // "pointId" seems to always contain the slotPath on 
            // the remote host.
            String slotPath = proxyExt.get("pointId").toString();

            // found it!
            if (slotPath.equals(remote.getSlotPath().toString()))
                return point;
        }

        // no such point
        return null;
    }

    private static int getControlPointKind(BControlPoint point)
    {
        if      (point instanceof BNumericPoint) return NUMERIC_KIND;
        else if (point instanceof BBooleanPoint) return BOOLEAN_KIND;
        else if (point instanceof BEnumPoint)    return ENUM_KIND;
        else if (point instanceof BStringPoint)  return STRING_KIND;

        else return UNKNOWN_KIND;
    }

    private static String makeStatusString(BStatus status)
    {
        if (status.isOk())
            return "ok";

        if (status.isDisabled())     return "disabled";
        if (status.isFault())        return "fault";
        if (status.isDown())         return "down";
        if (status.isAlarm())        return "alarm";
        if (status.isStale())        return "stale";
        if (status.isOverridden())   return "overridden";
        if (status.isNull())         return "null";
        if (status.isUnackedAlarm()) return "unackedAlarm";

        throw new IllegalStateException();
    }

////////////////////////////////////////////////////////////////
// Iterator
////////////////////////////////////////////////////////////////

    class CIterator implements Iterator
    {
        CIterator()
        {
            this.iterator = new ComponentTreeIterator(
                (BComponent) BOrd.make("slot:/").resolve(service, null).get());
            findNext();
        }

        public boolean hasNext() 
        { 
            return nextDict != null; 
        }

        public Object next()
        {
            if (nextDict == null) throw new IllegalStateException();

            HDict dict = nextDict;
            findNext();
            return dict;
        }

        public void remove() 
        { 
            throw new UnsupportedOperationException(); 
        }

        private void findNext()
        {
            nextDict = null;
            while (iterator.hasNext())
            {
                BComponent comp = (BComponent) iterator.next();

                if (isVisibleComponent(comp))
                {
                    nextDict = createComponentTags(comp);
                    break;
                }
            }
        }

        private final ComponentTreeIterator iterator;
        private HDict nextDict;
    }
}

