/*___INFO__MARK_BEGIN__*/
/*************************************************************************
 *
 *  The Contents of this file are made available subject to the terms of
 *  the Sun Industry Standards Source License Version 1.2
 *
 *  Sun Microsystems Inc., March, 2001
 *
 *
 *  Sun Industry Standards Source License Version 1.2
 *  =================================================
 *  The contents of this file are subject to the Sun Industry Standards
 *  Source License Version 1.2 (the "License"); You may not use this file
 *  except in compliance with the License. You may obtain a copy of the
 *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
 *
 *  Software provided under this License is provided on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
 *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 *  See the License for the specific provisions governing your rights and
 *  obligations concerning the Software.
 *
 *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
 *
 *   Copyright: 2009 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/
package com.sun.grid.jsv.examples;

import com.sun.grid.jsv.JsvManager;
import com.sun.grid.jsv.Jsv;
import com.sun.grid.jsv.JobDescription;
import java.io.IOException;
import java.util.Map;

/**
 * This is a simple JSV that:
 * <ul>
 * <li>Rejects binary jobs</li>
 * <li>Rejects parallel jobs that don't request a multiple of 16 processes</li>
 * <li>Temporarily reject jobs that request h_vmem</li>
 * <li>Removes request for h_data</li>
 * <li>Increments the job context variable "a"</li>
 * <li>Removes the job context variable "b"</li>
 * <li>Removes the job context variable "c"</li>
 * <li>Adds a job context variable d=5</li>
 * </ul>
 */
public class SimpleJsv implements Jsv {
    /**
     * Run this JSV.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SimpleJsv simple = new SimpleJsv();
        JsvManager jsv = new JsvManager();

        try {
            jsv.parse(simple);
        } catch (IOException e) {
            System.err.println("Unable to communication with client: " + e.getMessage());
        }
    }

    public void onStart(JsvManager jsv) {
        jsv.requestEnvironment(true);
    }

    public void onVerify(JsvManager jsv) {
        JobDescription params = jsv.getJobDescription();

        if ((params.isBinary() != null) && params.isBinary()) {
            jsv.reject("Binary job is rejected.");
        } else if ((params.getParallelEnvironment() != null) &&
                ((params.getParallelEnvironment().getRangeMin() % 16) != 0)) {
            jsv.reject("Parallel job does not request a multiple of 16 slots");
        } else {
            Map<String,String> hard = params.getHardResourceRequirements();
            boolean wait = false;
            boolean mod = false;

            if (hard != null) {
                if (hard.containsKey("h_vmem")) {
                    wait = true;
                }

                if (hard.remove("h_data") != null) {
                    mod = true;

                    if (params.getContext().equalsIgnoreCase("client")) {
                        jsv.log(JsvManager.LogLevel.INFO, "h_data as hard resource requirement has been deleted");
                    }

                    params.setHardResourceRequirements(hard);
                }
            }

            Map<String,String> context = params.getJobContext();

            if (context != null) {
                if (context.containsKey("a")) {
                    context.put("a", Integer.toString(Integer.parseInt(context.get("a")) + 1));
                } else {
                    context.put("a", "1");
                }

                context.remove("b");
                context.remove("c");
                context.put("d", "5");

                params.setJobContext(context);
            }

            if (wait) {
                jsv.rejectWait("Job is rejected. It might be submitted later.");
            } else if (mod) {
                jsv.modify("Job was modified before it was accepted.");
            } else {
                jsv.accept("Job is accepted.");
            }
        }
    }
}
