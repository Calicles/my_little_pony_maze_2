package com.antoine.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractImageTest {

    private AbstractImage image() {
        AbstractImage image= new AbstractImage() {};
        image.setImage("/ressources/images/tapis1.png");
        return image;
    }

    @Test
    public void getWidth() {
        assertEquals(32, image().getWidth());
    }

    @Test
    public void getHeight() {
        assertEquals(32, image().getHeight());
    }

    @Test
    public void getImage() {
        assertNotNull(image().getImage());
    }
}
