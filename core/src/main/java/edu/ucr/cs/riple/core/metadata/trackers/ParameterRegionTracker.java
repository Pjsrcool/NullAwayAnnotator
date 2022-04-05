package edu.ucr.cs.riple.core.metadata.trackers;

import edu.ucr.cs.riple.core.FixType;
import edu.ucr.cs.riple.core.metadata.AbstractRelation;
import edu.ucr.cs.riple.injector.Fix;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class ParameterRegionTracker extends AbstractRelation<TrackerNode> implements RegionTracker{

    private final FixType fixType;

    public ParameterRegionTracker() {
        super();
        this.fixType = FixType.PARAMETER;
    }

    @Override
    protected TrackerNode addNodeByLine(String[] values) {
        return null;
    }

    @Override
    public Set<Region> getRegions(Fix fix) {
        if (!fix.location.equals(fixType.name)) {
            return null;
        }
        return Collections.singleton(new Region(fix.method, fix.className));
    }
}
