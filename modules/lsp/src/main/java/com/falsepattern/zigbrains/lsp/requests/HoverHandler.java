/*
 * Copyright 2023 FalsePattern
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.falsepattern.zigbrains.lsp.requests;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.UIUtil;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Object used to process Hover responses
 */
public class HoverHandler {

    private Logger LOG = Logger.getInstance(HoverHandler.class);

    /**
     * Returns the hover string corresponding to a Hover response
     *
     * @param hover The Hover
     * @return The string response
     */
    public static String getHoverString(@NonNull Hover hover) {
        if (hover == null || hover.getContents() == null) {
            return "";
        }
        Either<List<Either<String, MarkedString>>, MarkupContent> hoverContents = hover.getContents();
        if (hoverContents.isLeft()) {
            List<Either<String, MarkedString>> contents = hoverContents.getLeft();
            if (contents != null && !contents.isEmpty()) {
                List<String> result = new ArrayList<>();
                for (Either<String, MarkedString> c : contents) {
                    String string = "";
                    if (c.isLeft() && !c.getLeft().isEmpty()) {
                        string = c.getLeft();
                    } else if (c.isRight()) {
                        MarkedString markedString = c.getRight();
                        string = (markedString.getLanguage() != null && !markedString.getLanguage().isEmpty()) ?
                                "```" + markedString.getLanguage() + " " + markedString.getValue() + "```" :
                                "";
                    }
                    Parser parser = Parser.builder().build();
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    if (!string.isEmpty()) {
                        result.add(renderer.render(parser.parse(string)));
                    }
                }
                return "<html><style>p {margin: 0; color: " + (UIUtil.isUnderDarcula() ? "rgb(187,187,187)" : "black") + ";</style>" + String.join("\n\n", result) + "</html>";
            } else {
                return "";
            }
        } else if (hoverContents.isRight()) {
            String markedContent = hoverContents.getRight().getValue();
            if (markedContent.isEmpty()) {
                return "";
            }
            Parser parser = Parser.builder().build();
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return "<html>" + renderer.render(parser.parse(markedContent)) + "</html>";
        } else {
            return "";
        }
    }
}
