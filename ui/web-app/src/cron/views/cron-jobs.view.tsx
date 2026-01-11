import { useState } from "react";
import { Container, Row, Col, Card, Badge, Button, Stack, Form, Accordion } from "react-bootstrap";
import { useServerObject, HttpErrorView, LoadingView, ReloadButton } from "spring-persistent-tasks-ui";
import { CronTriggerInfo } from "spring-persistent-tasks-ui";
import { useUrl } from "crossroad";
import * as Icon from "react-bootstrap-icons";

const CronJobsView = () => {
    const [_, setUrl] = useUrl();
    const [searchText, setSearchText] = useState("");

    const cronJobs = useServerObject<CronTriggerInfo[]>("/spring-tasks-api/cron-triggers", []);

    const doReload = () => {
        cronJobs.doGet();
    };

    // Filter cron jobs based on search text
    const filteredJobs = (cronJobs.data || []).filter((job) => {
        if (!searchText) return true;
        const search = searchText.toLowerCase();
        return (
            job.id.toLowerCase().includes(search) ||
            job.taskName.toLowerCase().includes(search) ||
            job.schedule.toLowerCase().includes(search) ||
            (job.tag && job.tag.toLowerCase().includes(search))
        );
    });

    const handleSuspendResume = async (job: CronTriggerInfo) => {
        const endpoint = `/spring-tasks-api/cron-triggers/${job.taskName}/${job.id}`;

        try {
            if (job.suspended) {
                // Resume the cron job
                await cronJobs.doCall(endpoint, { method: "POST" });
            } else {
                // Suspend the cron job
                await cronJobs.doCall(endpoint, { method: "DELETE" });
            }
            // Reload the list after successful operation
            doReload();
        } catch (error) {
            console.error("Failed to suspend/resume cron job:", error);
        }
    };

    const navigateToTriggers = (taskName: string, id: string) => {
        setUrl(`/task-ui/triggers?search=${encodeURIComponent(id)}&taskName=${encodeURIComponent(taskName)}`);
    };

    const navigateToHistory = (taskName: string, id: string) => {
        setUrl(`/task-ui/history?search=${encodeURIComponent(id)}&taskName=${encodeURIComponent(taskName)}`);
    };

    return (
        <Container fluid className="mt-3">
            <HttpErrorView error={cronJobs.error} />

            <Stack gap={3}>
                {/* Header */}
                <Row>
                    <Col>
                        <h3>
                            <Icon.ClockHistory className="me-2" />
                            Cron Jobs
                        </h3>
                        <p className="text-muted">
                            Manage scheduled cron triggers. View configurations, suspend/resume jobs, and navigate to their triggers or history.
                        </p>
                    </Col>
                </Row>

                {/* Search and Actions */}
                <Row className="align-items-center">
                    <Col md={6}>
                        <Form.Control
                            type="text"
                            placeholder="Search by ID, task name, schedule, or tag..."
                            value={searchText}
                            onChange={(e) => setSearchText(e.target.value)}
                        />
                    </Col>
                    <Col md={6} className="text-end">
                        <Stack direction="horizontal" gap={2} className="justify-content-end">
                            <Badge bg="secondary">
                                {filteredJobs.length} / {cronJobs.data?.length || 0} jobs
                            </Badge>
                            <ReloadButton
                                isLoading={cronJobs.isLoading}
                                onClick={doReload}
                            />
                        </Stack>
                    </Col>
                </Row>

                {/* Loading State */}
                {cronJobs.isLoading && !cronJobs.data && <LoadingView />}

                {/* Cron Jobs List */}
                {!cronJobs.isLoading && filteredJobs.length === 0 && (
                    <Card className="text-center p-5">
                        <Card.Body>
                            <Icon.InfoCircle size={48} className="text-muted mb-3" />
                            <h5>No Cron Jobs Found</h5>
                            <p className="text-muted">
                                {searchText
                                    ? "No cron jobs match your search criteria."
                                    : "No cron jobs are currently configured."}
                            </p>
                        </Card.Body>
                    </Card>
                )}

                {filteredJobs.length > 0 && (
                    <Accordion>
                        {filteredJobs.map((job, index) => (
                            <Accordion.Item eventKey={index.toString()} key={`${job.taskName}-${job.id}`}>
                                <Accordion.Header>
                                    <div className="d-flex align-items-center w-100 me-3">
                                        <div className="flex-grow-1">
                                            <div className="d-flex align-items-center gap-2">
                                                <strong>{job.id}</strong>
                                                <Badge bg={job.suspended ? "danger" : "success"}>
                                                    {job.suspended ? (
                                                        <>
                                                            <Icon.PauseFill className="me-1" />
                                                            Suspended
                                                        </>
                                                    ) : (
                                                        <>
                                                            <Icon.PlayFill className="me-1" />
                                                            Active
                                                        </>
                                                    )}
                                                </Badge>
                                                {job.tag && (
                                                    <Badge bg="info">
                                                        <Icon.TagFill className="me-1" />
                                                        {job.tag}
                                                    </Badge>
                                                )}
                                            </div>
                                            <div className="text-muted small mt-1">
                                                <Icon.Calendar className="me-1" />
                                                {job.schedule}
                                                <span className="ms-3">
                                                    <Icon.Gear className="me-1" />
                                                    {job.taskName}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </Accordion.Header>
                                <Accordion.Body>
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

                                            {/* Actions */}
                                            <div className="border-top pt-3">
                                                <Stack direction="horizontal" gap={2} className="flex-wrap">
                                                    <Button
                                                        variant={job.suspended ? "success" : "warning"}
                                                        size="sm"
                                                        onClick={() => handleSuspendResume(job)}
                                                        disabled={cronJobs.isLoading}
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
                                                        onClick={() => navigateToTriggers(job.taskName, job.id)}
                                                    >
                                                        <Icon.ListTask className="me-1" />
                                                        View Planned Triggers
                                                    </Button>

                                                    <Button
                                                        variant="outline-secondary"
                                                        size="sm"
                                                        onClick={() => navigateToHistory(job.taskName, job.id)}
                                                    >
                                                        <Icon.ClockHistory className="me-1" />
                                                        View History
                                                    </Button>
                                                </Stack>
                                            </div>
                                        </Card.Body>
                                    </Card>
                                </Accordion.Body>
                            </Accordion.Item>
                        ))}
                    </Accordion>
                )}
            </Stack>
        </Container>
    );
};

export default CronJobsView;
