package com.moodo.travel.board.dto;

import com.moodo.travel.board.Board;
import com.moodo.travel.common.file.CommonFile;
import java.util.List;
import java.util.Objects;

public class BoardDto {

    public static class BoardResponse {

        public Board board;
        public List<CommonFile> files;
        public int fileCount;

        public BoardResponse(Board board, List<CommonFile> files) {
            this.board = board;
            this.files = files;
            this.fileCount = Objects.isNull(files) ? 0 : files.size();
        }
    }
}