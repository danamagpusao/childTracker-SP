package com.example.ibdnmgps.childtracker2;

/**
 * Created by ibdnmgps on 6/29/2017.
 */

/*table created : multiple relation Child and Parent*/

public class ParentChild {
    protected long parent_id;
    protected long child_id;
    protected int _id;

    public ParentChild(){}

    public ParentChild(long parent_id, long child_id){
        this.parent_id = parent_id;
        this.child_id = child_id;
    }

    public void setParent(int parent_id){
        this.parent_id = parent_id;
    }

    public long getParent(){
        return this.parent_id;
    }

    public void setChild(int child_id) {
        this.child_id = child_id;
    }

    public long getChild(){
        return this.child_id;
    }

    public void setId(int id){
        this._id = id;
    }

    public int getId(){
        return this._id;
    }



}
