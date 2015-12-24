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
public class ERLDefaultEndIndicator implements EasyRefreshLoadLayout.Indicator {
    @Override
    public boolean canRefreshOrLoad(View mainView) {
        if(mainView instanceof ListView){
            ListView listView=(ListView)mainView;
            return (listView.getLastVisiblePosition()==listView.getChildCount()-1);
        }else if (mainView instanceof RecyclerView){
            RecyclerView recyclerView= (RecyclerView) mainView;
            RecyclerView.LayoutManager layoutManager=recyclerView.getLayoutManager();
             if(layoutManager instanceof StaggeredGridLayoutManager){
                StaggeredGridLayoutManager manager=(StaggeredGridLayoutManager)layoutManager;
                int spanPosition=manager.getSpanCount()-1;
                int[] positions=manager.findLastCompletelyVisibleItemPositions(null);
                int count =findMax(positions);
                return count==recyclerView.getAdapter().getItemCount()-1;
            }else if (layoutManager instanceof GridLayoutManager){
                GridLayoutManager manager=(GridLayoutManager) layoutManager;
                return (manager.findLastCompletelyVisibleItemPosition()==recyclerView.getAdapter().getItemCount()-1);
            }else if(layoutManager instanceof LinearLayoutManager){
                LinearLayoutManager manager= (LinearLayoutManager) layoutManager;
                return (manager.findLastCompletelyVisibleItemPosition()==recyclerView.getAdapter().getItemCount()-1);
            }
        }
        return false;
    }

    public int findMax(int[] data){
        int max=data[0];
        for(int temp:data){
            max=temp>max?temp:max;
        }
        return max;
    }


    @Override
    public int getOverBoundHeight(int scrollHeight) {
        return scrollHeight/4;
    }

    @Override
    public int getRefreshHeight(int scrollHeight) {
        return scrollHeight;
    }
}
