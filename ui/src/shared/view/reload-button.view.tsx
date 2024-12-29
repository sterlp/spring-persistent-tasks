import { forwardRef } from "react";
import { Button, Spinner } from "react-bootstrap";
import { ArrowClockwise } from "react-bootstrap-icons";

interface Props {
    isLoading: boolean;
    onClick: () => void;
}

const ReloadButton = forwardRef<HTMLButtonElement, Props & React.ButtonHTMLAttributes<HTMLButtonElement>>(
    ({ isLoading, onClick, ...props }, ref) => {
        return (
            <Button ref={ref} onClick={isLoading ? undefined : onClick} disabled={isLoading} {...props}>
                {isLoading ? (
                    <>
                        <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" />
                        <span className="visually-hidden">Loading...</span>
                    </>
                ) : (
                    <ArrowClockwise  size={18} />
                )}
            </Button>
        );
    }
);

export default ReloadButton;
