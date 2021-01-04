package nextstep.subway.line.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import nextstep.subway.line.exception.AlreadySavedLineException;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.exception.LessThanRemovableSizeException;
import nextstep.subway.station.exception.NotRegisteredStationException;

/**
 * @author : leesangbae
 * @project : subway
 * @since : 2020-12-30
 */
@Embeddable
public class LineStations {

    private final static int REMOVABLE_MINIMUM_SIZE = 2;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "line_id")
    private List<LineStation> lineStations = new ArrayList<>();

    public List<LineStation> getLineStations() {
        Optional<LineStation> preLineStation = lineStations.stream()
                .filter(it -> it.getPreStation() == null)
                .findFirst();

        List<LineStation> result = new LinkedList<>();
        while (preLineStation.isPresent()) {
            LineStation preStation = preLineStation.get();
            result.add(preStation);
            preLineStation = this.lineStations.stream()
                    .filter(it -> it.getPreStation() == preStation.getStation())
                    .findFirst();
        }
        return result;
    }

    public void add(LineStation lineStation) {

        if (this.lineStations.isEmpty()) {
            this.lineStations.add(lineStation);
            return;
        }

        addSectionValidate(lineStation);

        if (contains(lineStation.getPreStation())) {
            this.lineStations.stream()
                    .filter(it -> it.getPreStation() == lineStation.getPreStation())
                    .findFirst()
                    .ifPresent(it -> it.updatePreStation(lineStation.getStation(), lineStation.getDistance()));
            this.lineStations.add(lineStation);
            return;
        }

        if (contains(lineStation.getStation())) {
            this.lineStations.stream()
                    .filter(it -> it.getStation() == lineStation.getStation())
                    .findFirst()
                    .ifPresent(it -> it.updateStation(lineStation.getPreStation(), lineStation.getDistance()));
            this.lineStations.add(lineStation);
        }
    }

    public void remove(Station station) {

        removeSectionValidate();

        Optional<LineStation> lineStation = this.lineStations.stream()
                .filter(it -> station.equals(it.getPreStation()))
                .findFirst();

        Optional<LineStation> preLineStation = this.lineStations.stream()
                .filter(it -> station.equals(it.getStation()))
                .findFirst();

        if (preLineStation.isPresent() && lineStation.isPresent()) {
            LineStation mergedLineStation = mergeLineStation(preLineStation.get(), lineStation.get());
            this.lineStations.add(mergedLineStation);
        }
        preLineStation.ifPresent(value -> this.lineStations.remove(value));
        lineStation.ifPresent(value -> this.lineStations.remove(value));
        System.out.println("lineStation = " + lineStation);
    }

    private LineStation mergeLineStation(LineStation preLineStation, LineStation lineStation) {
        long totalDistance = preLineStation.getDistance() + lineStation.getDistance();
        return new LineStation(
                preLineStation.getLine(),
                lineStation.getStation(),
                preLineStation.getPreStation(),
                totalDistance
        );

    }


    private boolean contains(Station station) {
        return this.lineStations.stream()
                .anyMatch(lineStation -> lineStation.getStation() == station);
    }

    private void addSectionValidate(LineStation lineStation) {
        boolean upStationExist = contains(lineStation.getPreStation());
        boolean downStationExist = contains(lineStation.getStation());
        if (upStationExist && downStationExist) {
            throw new AlreadySavedLineException("이미 등록된 구간합니다.");
        }

        if (!upStationExist && !downStationExist) {
            throw new NotRegisteredStationException("등록할 수 없는 구간입니다.");
        }
    }

    private void removeSectionValidate() {
        int size = this.lineStations.size();
        if (size <= REMOVABLE_MINIMUM_SIZE) {
            throw new LessThanRemovableSizeException("구간 삭제 가능한 최소 사이즈보다 작습니다.");
        }
    }

}
