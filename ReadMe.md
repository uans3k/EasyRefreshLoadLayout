#How to use

        EasyRefreshLoadLayout layout=findViewById(R.id.easyrefresh);
        layout.addTop(topView,uiHandler,topInidicator);
        layout.addEnd(endView,uiHandler,bottomInidicator);

________________________________________

         EasyRefreshLoadLayout.UIHandler YourHandler extends EasyRefreshLoadLayout.UIHandler{
            @Override
            public void OnScroll(View scrollView, int mScrollY, int deltaY) {
                
            }

            @Override
            public void OnOverScroll(View scrollView, int mScrollY, int overScrollY, int deltaY) {

            }

            @Override
            public void OnRelease(View scrollView) {

            }

            @Override
            public void OnOverRelease(View scrollView) {

            }

            @Override
            public void OnReset(View scrollView) {

            }
        }

		 EasyRefreshLoadLayout.Indicator YourIndicator extends EasyRefreshLoadLayout.Indicator{
            @Override
            public boolean canRefreshOrLoad(View mainView) {
                return false;
            }

            @Override
            public int getOverBoundHeight(int scrollHeight) {
                return 0;
            }

            @Override
            public int getRefreshHeight(int scrollHeight) {
                return 0;
            }
        }