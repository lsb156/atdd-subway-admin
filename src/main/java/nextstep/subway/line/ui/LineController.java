package nextstep.subway.line.ui;

import java.util.List;
import javax.validation.Valid;
import nextstep.subway.line.application.LineService;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.LineUpdateRequest;
import nextstep.subway.line.dto.LinesResponse;
import nextstep.subway.line.dto.SectionCreateRequest;
import nextstep.subway.line.dto.SectionCreateResponse;
import nextstep.subway.line.dto.SectionDeleteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class LineController {
    private final LineService lineService;

    public LineController(final LineService lineService) {
        this.lineService = lineService;
    }

    @GetMapping("/lines")
    public ResponseEntity<?> showLines() {
        List<LinesResponse> lines = lineService.findAll();
        return ResponseEntity.ok(lines);
    }

    @GetMapping("/lines/{id}")
    public ResponseEntity<?> showLine(@PathVariable Long id) {
        LineResponse line = lineService.findById(id);
        return ResponseEntity.ok(line);
    }

    @PostMapping("/lines")
    public ResponseEntity<?> createLine(@RequestBody @Valid LineRequest lineRequest) {
        LineResponse line = lineService.saveLine(lineRequest);
        return ResponseEntity.created(URI.create("/lines/" + line.getId())).body(line);
    }

    @PutMapping("/lines/{id}")
    public ResponseEntity<?> updateLine(@PathVariable Long id, @RequestBody @Valid LineUpdateRequest request) {
        lineService.updateLine(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/lines/{id}")
    public ResponseEntity<?> deleteLine(@PathVariable Long id) {
        lineService.deleteLineById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lines/{id}/sections")
    public ResponseEntity<?> createSection(@PathVariable Long id,
            @RequestBody @Valid SectionCreateRequest request) {

        SectionCreateResponse section = lineService.addSection(id, request);
        return ResponseEntity.created(URI.create("/lines/" + section.getId())).body(section);
    }

    @DeleteMapping("/lines/{id}/sections")
    public ResponseEntity<?> deleteSection(@PathVariable Long id,
            @RequestBody @Valid SectionDeleteRequest request) {

        lineService.removeSection(id, request);
        return ResponseEntity.noContent().build();
    }


}
