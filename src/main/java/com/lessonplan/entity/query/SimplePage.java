package com.lessonplan.entity.query;

import java.io.Serializable;

/**
 * 简单分页参数
 */
public class SimplePage implements Serializable {

    private static final long serialVersionUID = 1L;

    private int pageNo;
    private int countTotal;
    private int pageSize;
    private int pageTotal;
    private int start;
    private int end;

    public SimplePage() {
    }

    public SimplePage(Integer pageNo, int countTotal, int pageSize) {
        if (pageNo == null || pageNo <= 0) {
            pageNo = 1;
        }
        this.pageNo = pageNo;
        this.countTotal = countTotal;
        this.pageSize = pageSize;
        this.pageTotal = (int) Math.ceil((double) countTotal / pageSize);
        if (this.pageTotal == 0) {
            this.pageTotal = 1;
        }
        if (this.pageNo > this.pageTotal) {
            this.pageNo = this.pageTotal;
        }
        this.start = (this.pageNo - 1) * this.pageSize;
        this.end = this.pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getCountTotal() {
        return countTotal;
    }

    public void setCountTotal(int countTotal) {
        this.countTotal = countTotal;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(int pageTotal) {
        this.pageTotal = pageTotal;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
