package com.googlecalendar;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter implements Filterable {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;
    // 필터링된 결과 데이터를 저장하기 위한 ArrayList. 최초에는 전체 리스트 보유.
    private ArrayList<ListViewItem> filteredItemList = listViewItemList ;

    Filter listFilter ;
    TextView textView01,  textView02,  textView03,  textView04,  textView05;

    // ListViewAdapter의 생성자
    public ListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return filteredItemList.size() ;
    }

    public int getFilteredItemId(int position) {
        int a = filteredItemList.get(position).getId();
        return a;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cal_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        textView01 = (TextView) convertView.findViewById(R.id.textView01) ;
        textView02 = (TextView) convertView.findViewById(R.id.textView02) ;
        textView03 = (TextView) convertView.findViewById(R.id.textView03) ;
        textView04 = (TextView) convertView.findViewById(R.id.textView04) ;
        textView05 = (TextView) convertView.findViewById(R.id.textView05) ;

        // Data Set(filteredItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = filteredItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        textView01.setText(listViewItem.getName());
        textView02.setText(listViewItem.getDate());
        textView03.setText(listViewItem.getAl());
        textView04.setText(listViewItem.getMap());
        textView05.setText(String.valueOf(listViewItem.getId()));
        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return filteredItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(int id, String t1, String t2, String t3, String t4) {
        ListViewItem item = new ListViewItem();

        item.setId(id);
        item.setName(t1);
        item.setDate(t2);
        item.setAl(t3);
        item.setMap(t4);
        listViewItemList.add(item);
    }


    @Override
    public Filter getFilter() {
        if(listFilter == null){
            listFilter = new ListFilter();
        }
        return listFilter;
    }
/*
    public void addItem2(ArrayList<ListViewItem> result) {
        textView01.setText(result.getName());
        textView02.setText(result.getDate());
        textView03.setText(result.getAl());
        textView04.setText(result.getMap());
    }*/

    private class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults() ;               // 검색 결과
            if (constraint == null || constraint.length() == 0) {       // 검색 결과가 없거나 길이가 0이면
                results.values = listViewItemList ;                 // 원래 리스트 뷰가 filter 리스트뷰의 리스트뷰
                results.count = listViewItemList.size() ;            // 사이즈도 원래 리스트 뷰 사이즈
            }
            else {                                                                  // 검색 값이 있으면
                ArrayList<ListViewItem> itemList = new ArrayList<ListViewItem>() ;        // 새로 만듬
                for (ListViewItem item : listViewItemList) {                        // 리스트 뷰가 있는 만큼 아이템 추가
                    if (item.getName().toUpperCase().contains(constraint.toString().toUpperCase())) {     //리스트 뷰의 텍스트에서 찾기
                        itemList.add(item);     // 있으면 추가
                    }
                }
                results.values = itemList ;         // 리스트 뷰 값
                results.count = itemList.size() ;       // 수
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {     // 결과

            // update listview by filtered data list.
            filteredItemList = (ArrayList<ListViewItem>) results.values ;           // 리스트 뷰 업데이트
            // notify
            if (results.count > 0) {                        // 0보다 크면 업데이트
                notifyDataSetChanged() ;
            } else {
                notifyDataSetInvalidated() ;
            }
        }
    }
}