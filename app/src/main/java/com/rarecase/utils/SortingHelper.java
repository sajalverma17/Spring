package com.rarecase.utils;

import com.rarecase.model.Song;
import java.util.List;

public class SortingHelper {

    /**
     * Sorts a {@code List} of {@code Song}s using Quicksort.
     */
    public static List<Song> quickSort(List<Song> input, int start, int end){
        List<Song> partitionedList = input;
        if(start < end) {
            Song pivot = input.get(start);
            int splitPoint = getPartitionSplitPoint(input, pivot, start, end);
            partitionedList = getPartitionedList(input, pivot, start, end);
            partitionedList = quickSort(partitionedList,start,splitPoint-1);
            partitionedList = quickSort(partitionedList,splitPoint+1,end);
        }
        return partitionedList;
    }

    private static int getPartitionSplitPoint(List<Song> input,Song pivot,int left,int right) {
        int index = left;
        while (left <= right && index<right) {
            while ((left<=right && (input.get(left).getSong().compareToIgnoreCase(pivot.getSong()) <= 0))){
                left++;
            }
            while (right>=left && (input.get(right).getSong().compareToIgnoreCase(pivot.getSong()) >= 0)){
                right--;
            }
            if(left>right) {
                break;
            }else{
                Song temp = input.get(left);
                input.set(left,input.get(right));
                input.set(right,temp);
            }
            index++;
        }
        return right;
    }

    private static List<Song> getPartitionedList(List<Song> input,Song pivot,int left,int right) {
        int index = left;
        int originalLeft = left;
        while (left <= right && index<right) {

            while ( left<=right && (input.get(left).getSong().compareToIgnoreCase(pivot.getSong()) <= 0)){
                left++;
            }
            while ( right>=left && (input.get(right).getSong().compareToIgnoreCase(pivot.getSong()) >= 0)){
                right--;
            }
            if(left>right){
                Song temp = input.get(originalLeft);
                input.set(originalLeft,input.get(right));
                input.set(right,temp);
                break;
            }
            else{
                Song temp = input.get(left);
                input.set(left,input.get(right));
                input.set(right,temp);
            }
            index++;
        }
        return input;
    }

}
