package com.example.shortestpath;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.shortestpath.PositionData.lineOneArr;
import static com.example.shortestpath.PositionData.lineSixOneArr;
import static com.example.shortestpath.PositionData.lineSixTwoArr;
import static com.example.shortestpath.PositionData.lineThreeArr;
import static com.example.shortestpath.PositionData.lineTwoArr;

public class MainActivity extends AppCompatActivity {
    String endPosition="大学城";
    String startPosition="";
    //List<SiteNode> firstNodeList;
    List<SiteNode> tempNodeList;
    SiteNode firstNode;
    SiteNode lineOneHeadSite=new SiteNode();
    SiteNode lineTwoHeadSite=new SiteNode();
    SiteNode lineThreeHeadSite=new SiteNode();
    SiteNode lineSixOneHeadSite=new SiteNode();
    SiteNode lineSixTwoHeadSite=new SiteNode();

    Map<String,List<SiteNode>> siteNodeMap=new HashMap<>();
    TextView shortPath;
    TextView pathCount;
    EditText siteStart;
    EditText siteEnd;
    Button findButton;
    int pCount=0;
    /*
    * 大概流程：
    * 将每条路线生成一条链表，根据站名放入对应站点到map里，因为一个站点可能在不同的路线上，所以map里存放Node的List
    * 从起始点开始进行查找，分别从父方向与子方向进行，如果遇到多条路线的节点，则取出对应的Node然后递归
    * 构建List用来存放途中所经过的站点，如果能找到终点位置，则返回相应的List，否则返回null
    * 为了不让查找死循环，构建一个List记录之前所经过的点，并传入递归查找中，如果当前站点已在List中，则结束查找返回null
    * 每次查找到终点位置后，记录一次（pCount++），代表总的路线数
    * 因为查找返回的list记录了所有经过的站点信息，所以记录最少的点即为最短路线。
    *
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shortPath= (TextView) findViewById(R.id.path_shotest);
        pathCount= (TextView) findViewById(R.id.path_count);
        siteStart= (EditText) findViewById(R.id.start_site);
        siteEnd= (EditText) findViewById(R.id.end_site);
        findButton= (Button) findViewById(R.id.find_button);
        initHeadSite();
        //建立链表路线
        buildLineLink(lineOneHeadSite,lineOneArr,1);
        buildLineLink(lineTwoHeadSite,lineTwoArr,2);
        buildLineLink(lineThreeHeadSite,lineThreeArr,3);
        buildLineLink(lineSixOneHeadSite,lineSixOneArr,6);
        buildLineLink(lineSixTwoHeadSite,lineSixTwoArr,6);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosition=siteStart.getText().toString().trim();
                endPosition=siteEnd.getText().toString().trim();
                final List<SiteNode> nodeList=siteNodeMap.get(startPosition);
                if(nodeList==null||siteNodeMap.get(endPosition)==null){
                    Toast.makeText(MainActivity.this,"请输入有效的地址",Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int m=0;
                        pCount=0;
                        for(int i=0;i<nodeList.size();i++){
                            List<SiteNode> currentNodeList=findTopNode(nodeList.get(i),new ArrayList<SiteNode>());
                            if(m==0&&currentNodeList.size()>0){
                                m++;
                                firstNode=nodeList.get(i);
                                tempNodeList=currentNodeList;
                            }else if(currentNodeList.size()>0&&m>0){
                                m++;
                                if(tempNodeList.size()>currentNodeList.size()){
                                    tempNodeList=currentNodeList;
                                    firstNode=nodeList.get(i);
                                }
                            }else {
                                tempNodeList=new ArrayList<SiteNode>();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(pCount==0||startPosition==endPosition){
                                    pathCount.setText("无路线");
                                    shortPath.setText("");
                                }else {
                                    if(tempNodeList.size()==0){
                                        Toast.makeText(MainActivity.this,"长度为0",Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    StringBuilder str=new StringBuilder();
                                    str.append("☆起始站点："+firstNode.siteName+"    "+firstNode.lineIn+"号线\n");
                                    for (int i=0;i<tempNodeList.size();i++){
                                        if(i<tempNodeList.size()-1){
                                            str.append("↓经过站点："+tempNodeList.get(i).siteName+"    "+tempNodeList.get(i).lineIn+"号线\n");
                                            continue;
                                        }
                                        str.append("★目标站点："+tempNodeList.get(i).siteName+"    "+tempNodeList.get(i).lineIn+"号线\n");
                                    }
                                    pathCount.setText("共找到"+pCount+"条路线，"+"其中最短路线为：");
                                    shortPath.setText(str);
                                }
                            }
                        });
                    }
                }).start();
            }
        });


    }
    //节点查找进入
    public List<SiteNode> findTopNode(SiteNode siteNode,List<SiteNode> lastLevelNodeList){
        List<SiteNode> currentAllNodeList=new ArrayList<>();
        List<SiteNode> tempLastNodeList=new ArrayList<>();
        //复制上一层已存在的点
        tempLastNodeList.addAll(lastLevelNodeList);
        tempLastNodeList.add(siteNode);
        //
        System.out.println("AAA--------------进入节点查找--------------"+lastLevelNodeList.size());
        List<SiteNode> parentResultList=findParent(siteNode.siteParent,tempLastNodeList);
        List<SiteNode> childResultList=findChild(siteNode.siteChild,tempLastNodeList);
        System.out.println("AAA--------------当前节点查找结束--------------"+lastLevelNodeList.size());
        if(childResultList==null&&parentResultList!=null){
            currentAllNodeList.addAll(parentResultList);
            System.out.println("AAAC------------找到父方向并返回--------------");
            return parentResultList;
        }

        else if(childResultList!=null&&parentResultList==null){
            currentAllNodeList.addAll(childResultList);
            System.out.println("AAAC-------------找到子方向并返回----------------");
            return childResultList;

        }
        else if (childResultList!=null&&parentResultList!=null){
            System.out.println("找到两个方向："+childResultList.size()+"father:"+parentResultList.size());
            if(childResultList.size()>parentResultList.size()){
                System.out.println("AAAC-------------返回了父方向-----------------");
                return parentResultList;
            }else {
                System.out.println("AAAC--------------返回了子方向----------------");
                return childResultList;
            }
        } else {
            System.out.println("AAA---------------节点没有查找到路径---------------");
            return currentAllNodeList;
        }
    }
    //寻找父方向的路径
    public List<SiteNode> findParent(SiteNode siteNode,List<SiteNode> latsLevelNodeList){
        List<SiteNode> parentNodeList=new ArrayList<>();
        SiteNode parentTempNode=siteNode;
        while (parentTempNode!=null){
            System.out.println("AAAP"+parentTempNode.siteName+" "+siteNode.siteName);
            if(!parentNodeList.contains(parentTempNode)&&!latsLevelNodeList.contains(parentTempNode)){
                if(parentTempNode.siteName.equals(endPosition)){
                    pCount++;
                    System.out.println("AAAC------------------父方向查找成功---------------------------");
                    parentNodeList.add(parentTempNode);
                    return parentNodeList;
                }else if(siteNodeMap.get(parentTempNode.siteName).size()==1){
                    parentNodeList.add(parentTempNode);
                    parentTempNode=parentTempNode.siteParent;
                }else {
                    parentNodeList.add(parentTempNode);
                    List<SiteNode> newTopNode=siteNodeMap.get(parentTempNode.siteName);
                    latsLevelNodeList.addAll(latsLevelNodeList.size(),parentNodeList);
                    int m=0;
                    List<SiteNode> tempNodeList=new ArrayList<>();
                    for(int i=0;i<newTopNode.size();i++){
                        List<SiteNode> currentNodeList=findTopNode(newTopNode.get(i),latsLevelNodeList);
                        if(m==0&&currentNodeList.size()>0){
                            m++;
                            tempNodeList=currentNodeList;
                        }else if(currentNodeList.size()>0&&m>0){
                            if(tempNodeList.size()>currentNodeList.size()){
                                tempNodeList=currentNodeList;
                            }
                        }
                    }
                    System.out.println("AAAC--------------父方向遇节点查找结束------------");
                    if(m==0){
                        System.out.println("AAAC-------------父方向遇节点无结果--------------");
                        return null;
                    }
                    parentNodeList.addAll(parentNodeList.size(),tempNodeList);
                    return parentNodeList;
                }

            }else {
                return null;
            }
        }
        return null;
    }
    //寻找子方向的路径
    public List<SiteNode> findChild(SiteNode siteNode,List<SiteNode> latsLevelNodeList){
        List<SiteNode> childNodeList=new ArrayList<>();
        SiteNode childTempNode=siteNode;
        while (childTempNode!=null){
            System.out.println("AAAC"+childTempNode.siteName+" "+siteNode.siteName);
            if(!childNodeList.contains(childTempNode)&&!latsLevelNodeList.contains(childTempNode)){
                if(childTempNode.siteName.equals(endPosition)){
                    childNodeList.add(childTempNode);
                    pCount++;
                    System.out.println("AAAC------------------子方向查找成功---------------------------");
                    return childNodeList;
                }else if(siteNodeMap.get(childTempNode.siteName).size()==1){
                    childNodeList.add(childTempNode);
                    childTempNode=childTempNode.siteChild;
                }else {
                    childNodeList.add(childTempNode);
                    latsLevelNodeList.addAll(latsLevelNodeList.size(),childNodeList);
                    List<SiteNode> newTopNode=siteNodeMap.get(childTempNode.siteName);
                    int m=0;
                    List<SiteNode> tempNodeList=new ArrayList<>();
                    for(int i=0;i<newTopNode.size();i++){
                        List<SiteNode> currentNodeList=findTopNode(newTopNode.get(i),latsLevelNodeList);
                        if(m==0&&currentNodeList.size()>0){
                            m++;
                            tempNodeList=currentNodeList;
                        }else if(currentNodeList.size()>0&&m>0){
                            if(tempNodeList.size()>currentNodeList.size()){
                                tempNodeList=currentNodeList;
                            }
                        }
                    }
                    if(m==0){
                        System.out.println("AAAC----------------子方向遇节点查找无结果-----------------");
                        return null;
                    }
                    System.out.println("AAAC-----------------子方向遇节点查找有结果-----------------");
                    childNodeList.addAll(childNodeList.size(),tempNodeList);
                    return childNodeList;
                }
            }else {
                return null;
            }
        }
        return null;
    }
    //初始化头节点
    public void initHeadSite(){
        lineOneHeadSite.siteName=lineOneArr[0];
        lineOneHeadSite.lineIn=1;
        List<SiteNode> oneList=new ArrayList<>();
        oneList.add(lineOneHeadSite);
        siteNodeMap.put(lineOneArr[0],oneList);
        //
        lineTwoHeadSite.siteName=lineTwoArr[0];
        lineTwoHeadSite.lineIn=2;
        List<SiteNode> twoList=new ArrayList<>();
        twoList.add(lineTwoHeadSite);
        siteNodeMap.put(lineTwoArr[0],twoList);
        //
        lineThreeHeadSite.siteName=lineThreeArr[0];
        lineThreeHeadSite.lineIn=3;
        //与2号线头节点一样
        twoList.add(lineThreeHeadSite);
        siteNodeMap.put(lineThreeArr[0],twoList);
        //
        lineSixOneHeadSite.siteName=lineSixOneArr[0];
        lineSixOneHeadSite.lineIn=6;
        List<SiteNode> sixOneList=new ArrayList<>();
        sixOneList.add(lineSixOneHeadSite);
        siteNodeMap.put(lineSixOneArr[0],sixOneList);
        //
        lineSixTwoHeadSite.siteName=lineSixTwoArr[0];
        lineSixTwoHeadSite.lineIn=6;
        List<SiteNode> sixTwoList=new ArrayList<>();
        sixTwoList.add(lineSixTwoHeadSite);
        siteNodeMap.put(lineSixTwoArr[0],sixTwoList);
    }
    //创建路线链表
    public void buildLineLink(SiteNode head,String[] siteData,int lineIn){
        SiteNode tempNode=head;
        for(int i=1;i<siteData.length;i++){
            SiteNode siteNode=new SiteNode();
            siteNode.siteName=siteData[i];
            siteNode.lineIn=lineIn;
            siteNode.siteParent=tempNode;
            tempNode.siteChild=siteNode;
            tempNode=siteNode;
            List<SiteNode> nodeList=siteNodeMap.get(siteData[i]);
            if(nodeList==null){
                nodeList=new ArrayList<>();
            }
            nodeList.add(siteNode);
            siteNodeMap.put(siteData[i],nodeList);
        }
    }
}
