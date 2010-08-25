/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.logging.internal;

import org.gradle.api.Action;
import org.gradle.api.UncheckedIOException;
import org.gradle.logging.StyledTextOutput;
import org.gradle.util.LineBufferingOutputStream;
import org.gradle.util.TimeProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link org.gradle.logging.StyledTextOutput} implementation which generates events of type {@link
 * org.gradle.logging.internal.StyledTextOutputEvent}. This implementation is not thread-safe.
 */
public class LoggingBackedStyledTextOutput extends AbstractStyledTextOutput {
    private final LineBufferingOutputStream outstr;
    private Style style = Style.Normal;
    private boolean styleChange;

    public LoggingBackedStyledTextOutput(OutputEventListener listener, String category, TimeProvider timeProvider) {
        outstr = new LineBufferingOutputStream(new LogAction(listener, category, timeProvider), true);
    }

    public StyledTextOutput style(Style style) {
        styleChange = true;
        try {
            outstr.flush();
        } finally {
            styleChange = false;
        }
        this.style = style;
        return this;
    }

    public StyledTextOutput text(Object text) {
        try {
            outstr.write(text.toString().getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    private class LogAction implements Action<String> {
        private final OutputEventListener listener;
        private final String category;
        private final TimeProvider timeProvider;
        private List<StyledTextOutputEvent.Span> spans;

        public LogAction(OutputEventListener listener, String category, TimeProvider timeProvider) {
            this.listener = listener;
            this.category = category;
            this.timeProvider = timeProvider;
        }

        public void execute(String text) {
            if (text.length() == 0) {
                return;
            }

            StyledTextOutputEvent.Span span = new StyledTextOutputEvent.Span(style, text);
            if (styleChange) {
                if (spans == null) {
                    spans = new ArrayList<StyledTextOutputEvent.Span>();
                }
                spans.add(span);
                return;
            } else if (spans != null) {
                spans.add(span);
            } else {
                spans = Collections.singletonList(span);
            }

            StyledTextOutputEvent event = new StyledTextOutputEvent(timeProvider.getCurrentTime(), category, spans);
            spans = null;
            
            listener.onOutput(event);
        }
    }
}