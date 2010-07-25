/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.view;

public class MainMenu
        extends Menu {

    private static final long serialVersionUID = 1L;

    public static final String HOME = "home";

    public static final String SPACES = "spaces";

    public static final String SERVICES = "services";

    private static Menu instance;

    private MainMenu() {
        addMenuItem(HOME, "/", "home");
        addMenuItem(SPACES, "/spaces.htm", "spaces");
        addMenuItem(SERVICES, "/services", "services");
    }

    public static Menu instance() {
        if (instance == null) {
            instance = new MainMenu();
        }
        return instance;
    }

}
