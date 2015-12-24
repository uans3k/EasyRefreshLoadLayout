package cn.uans3k.view.viewgroup;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Administrator on 2015/10/10.
 */
public class ERLDefaultTopIndicator implements EasyRefreshLoadLayout.Indicator {

    @Override
    public boolean canRefreshOrLoad(View mainView) {

        if(mainView instanceof ListView){
            ListView listView=(ListView)mainView;
            return (listView.getFirstVisiblePosition()==0);
        }else if (mainView instanceof RecyclerView){
            RecyclerView recyclerView= (RecyclerView) mainView;
            RecyclerView.LayoutManager layoutManager=recyclerView.getLayoutManager();
            if(layoutManager instanceof StaggeredGridLayoutManager){
                StaggeredGridLayoutManager manager=(StaggeredGridLayoutManager)layoutManager;
                return (manager.findFirstCompletelyVisibleItemPositions(null)[0]==0);
            }else if (layoutManager instanceof GridLayoutManager){
                GridLayoutManager manager=(GridLayoutManager) layoutManager;
                return (manager.findFirstCompletelyVisibleItemPosition()==0);
            }else if(layoutManager instanceof LinearLayoutManager){
                LinearLayoutManager manager= (LinearLayoutManager) layoutManager;
                return (manager.findFirstCompletelyVisibleItemPosition()==0);
            }
        }
        return false;
    }

    @Override
    public int getOverBoundHeight(int scrollHeight) {
        return scrollHeight/2;
    }

    @Override
    public int getRefreshHeight(int scrollHeight) {
        return scrollHeight/2;
    }


}
