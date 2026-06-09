package com.randomperson22.rdbc.teavm;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

public class ChromebookSystem extends ApplicationAdapter {

    private HtmlClient client;

    @Override
    public void create() {
        client = new HtmlClient();
        client.connect();
    }

    @Override
    public void render() {}
}
