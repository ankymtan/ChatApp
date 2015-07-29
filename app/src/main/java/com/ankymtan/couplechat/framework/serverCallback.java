package com.ankymtan.couplechat.framework;

import com.ankymtan.couplechat.entity.User;

/**
 * Created by ankym on 23/6/2015.
 */
public interface serverCallback {
    public abstract void done(User returnedUser);
}
