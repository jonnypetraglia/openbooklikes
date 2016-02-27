package com.qweex.openbooklikes;




public interface LoadingViewInterface {

    enum State {
        INITIAL,
        MORE
    }

    void show();

    void show(String text);

    void content();

    void empty();

    void error(Throwable error);

    void hide();

    void changeState(State s);
}
