import { Card, Row, Col, Badge, Button, Stack } from "react-bootstrap";
import { CronTriggerInfo } from "spring-persistent-tasks-ui";
import * as Icon from "react-bootstrap-icons";

interface Props {
    job: CronTriggerInfo;
    isLoading: boolean;
    onSuspendResume: (job: CronTriggerInfo) => void;
    onViewTriggers: (taskName: string, id: string) => void;
    onViewHistory: (taskName: string, id: string) => void;
}

const CronJobCard = ({
    job,
    isLoading,
    onSuspendResume,
    onViewTriggers,
    onViewHistory,
}: Props) => {
    return (
        <Card className="border-0 shadow-sm">
            <Card.Body>
                <Row className="mb-3">
                    <Col md={6}>
                        <div className="mb-2">
                            <small className="text-muted">Cron ID</small>
                            <div className="fw-semibold">{job.id}</div>
                        </div>
                        <div className="mb-2">
                            <small className="text-muted">Task Name</small>
                            <div className="fw-semibold">{job.taskName}</div>
                        </div>
                        <div className="mb-2">
                            <small className="text-muted">Schedule</small>
                            <div className="fw-semibold">
                                <code>{job.schedule}</code>
                            </div>
                        </div>
                    </Col>
                    <Col md={6}>
                        <div className="mb-2">
                            <small className="text-muted">Priority</small>
                            <div className="fw-semibold">{job.priority}</div>
                        </div>
                        <div className="mb-2">
                            <small className="text-muted">Tag</small>
                            <div className="fw-semibold">{job.tag || "—"}</div>
                        </div>
                        <div className="mb-2">
                            <small className="text-muted">Has State Provider</small>
                            <div className="fw-semibold">
                                {job.hasStateProvider ? (
                                    <Badge bg="success">Yes</Badge>
                                ) : (
                                    <Badge bg="secondary">No</Badge>
                                )}
                            </div>
                        </div>
                    </Col>
                </Row>

                <div className="border-top pt-3">
                    <Stack direction="horizontal" gap={2} className="flex-wrap">
                        <Button
                            variant={job.suspended ? "success" : "warning"}
                            size="sm"
                            onClick={() => onSuspendResume(job)}
                            disabled={isLoading}
                        >
                            {job.suspended ? (
                                <>
                                    <Icon.PlayFill className="me-1" />
                                    Resume
                                </>
                            ) : (
                                <>
                                    <Icon.PauseFill className="me-1" />
                                    Suspend
                                </>
                            )}
                        </Button>

                        <Button
                            variant="outline-primary"
                            size="sm"
                            onClick={() => onViewTriggers(job.taskName, job.id)}
                        >
                            <Icon.ListTask className="me-1" />
                            View Planned Triggers
                        </Button>

                        <Button
                            variant="outline-secondary"
                            size="sm"
                            onClick={() => onViewHistory(job.taskName, job.id)}
                        >
                            <Icon.ClockHistory className="me-1" />
                            View History
                        </Button>
                    </Stack>
                </div>
            </Card.Body>
        </Card>
    );
};

export default CronJobCard;
