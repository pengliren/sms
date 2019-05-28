package com.sms.server.net.http.message;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Case Ignoring Comparator
 * @author pengliren
 *
 */
public class CaseIgnoringComparator implements Comparator<String>, Serializable {

    private static final long serialVersionUID = 4582133183775373862L;

    static final CaseIgnoringComparator INSTANCE = new CaseIgnoringComparator();

    private CaseIgnoringComparator() {
    }

    @Override
    public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
