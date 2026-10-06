package com.cimdy.coe.data.gen.talent;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.node.JewelNodes01;
import com.cimdy.coe.data.gen.talent.node.KeystoneNodes01;
import com.cimdy.coe.data.gen.talent.node.minor.*;
import com.cimdy.coe.data.gen.talent.node.notable.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TalentNodeGroups {
    public static final Map<String, List<TalentNodeDef>> GROUPS = new LinkedHashMap<>();

    static {
        GROUPS.put("keystone/KeystoneNodes01", KeystoneNodes01.build());
        GROUPS.put("minor/MinorNodes01", MinorNodes01.build());
        GROUPS.put("minor/MinorNodes02", MinorNodes02.build());
        GROUPS.put("minor/MinorNodes03", MinorNodes03.build());
        GROUPS.put("minor/MinorNodes04", MinorNodes04.build());
        GROUPS.put("minor/MinorNodes05", MinorNodes05.build());
        GROUPS.put("minor/MinorNodes06", MinorNodes06.build());
        GROUPS.put("minor/MinorNodes07", MinorNodes07.build());
        GROUPS.put("minor/MinorNodes08", MinorNodes08.build());
        GROUPS.put("minor/MinorNodes09", MinorNodes09.build());
        GROUPS.put("minor/MinorNodes10", MinorNodes10.build());
        GROUPS.put("minor/MinorNodes11", MinorNodes11.build());
        GROUPS.put("minor/MinorNodes12", MinorNodes12.build());
        GROUPS.put("minor/MinorNodes13", MinorNodes13.build());
        GROUPS.put("minor/MinorNodes14", MinorNodes14.build());
        GROUPS.put("minor/MinorNodes15", MinorNodes15.build());
        GROUPS.put("minor/MinorNodes16", MinorNodes16.build());
        GROUPS.put("minor/MinorNodes17", MinorNodes17.build());
        GROUPS.put("minor/MinorNodes18", MinorNodes18.build());
        GROUPS.put("minor/MinorNodes19", MinorNodes19.build());
        GROUPS.put("minor/MinorNodes20", MinorNodes20.build());
        GROUPS.put("minor/MinorNodesAttr", MinorNodesAttr.build());
        GROUPS.put("notable/NotableNodes01", NotableNodes01.build());
        GROUPS.put("notable/NotableNodes02", NotableNodes02.build());
        GROUPS.put("notable/NotableNodes03", NotableNodes03.build());
        GROUPS.put("notable/NotableNodes04", NotableNodes04.build());
        GROUPS.put("notable/NotableNodes05", NotableNodes05.build());
        GROUPS.put("notable/NotableNodes06", NotableNodes06.build());
        GROUPS.put("notable/NotableNodes07", NotableNodes07.build());
        GROUPS.put("jewel/JewelNodes01", JewelNodes01.build());
    }
}
