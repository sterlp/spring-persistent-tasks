import { Button, Spinner } from "react-bootstrap";
import { ArrowClockwise } from "react-bootstrap-icons";

interface Props {
    isLoading: boolean;
    onClick: () => void | any;
}
function ReloadButton({ isLoading, onClick }: Props) {
    if (isLoading) return <Spinner animation="border" role="status"></Spinner>;
    return (
        <Button onClick={onClick}>
            <ArrowClockwise />
        </Button>
    );
}

export default ReloadButton;
