package com.example.shortestpath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-12-20.
 */

public class SiteNode {
    public String siteName;
    public int lineIn;
    private List<Integer> otherLineIn=new ArrayList<>();
    public SiteNode siteParent;
    public SiteNode siteChild;
    public int parentDirection=0;
    public int childDirection=0;
}
