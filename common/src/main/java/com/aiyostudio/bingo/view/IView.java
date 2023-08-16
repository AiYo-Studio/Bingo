package com.aiyostudio.bingo.view;

import java.util.List;
import java.util.Map;

public interface IView {

    void call();

    void onPreInit();

    void onPostInit();

    void initializeDisplayItem();

    void initializeQuestItem();

    Map<String, List<String>[]> initializeStateItem();
}
