package org.xhy.community.infrastructure.markdown.impl;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

/**
 * 自定义视频内联节点：!video[alt](src){poster=...}
 */
public class VideoNode extends Node {
    private final String src;
    private final String poster;

    public VideoNode(String src, String poster) {
        this.src = src;
        this.poster = poster;
    }

    public String getSrc() { return src; }
    public String getPoster() { return poster; }

    @Override
    public BasedSequence[] getSegments() {
        return EMPTY_SEGMENTS;
    }
}

