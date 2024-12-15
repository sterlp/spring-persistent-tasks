import { Button, Spinner } from "react-bootstrap";
import { ArrowClockwise } from "react-bootstrap-icons";

interface Props {
    isLoading: boolean;
    onClick: () => void | any;
}
const ReloadButton = ({ isLoading, onClick }: Props) => {
    if (isLoading)
        return (
            <Button disabled>
                <Spinner
                    as="span"
                    animation="border"
                    size="sm"
                    role="status"
                    aria-hidden="true"
                />
                <span className="visually-hidden">Loading...</span>
            </Button>
        );

    return (
        <Button onClick={onClick}>
            <ArrowClockwise size={18} />
        </Button>
    );
};

export default ReloadButton;
