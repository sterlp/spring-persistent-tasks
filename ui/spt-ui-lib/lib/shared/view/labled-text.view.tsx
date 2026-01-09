import type { ReactNode } from "react";

interface Props {
    label: string;
    value?: string | number | ReactNode;
    className?: string;
    onClick?: () => void;
}
const LabeledText: React.FC<Props> = ({ label, value, className, onClick }) => {
    return (
        <div className={className}>
            <small className="text-muted d-block mb-1">
                {label}
            </small>
            {onClick ? (
                <a
                    onClick={(e) => {
                        e.preventDefault();
                        onClick();
                    }}
                    href="#"
                    className="text-decoration-none fw-semibold"
                >
                    {value}
                </a>
            ) : (
                <div className="fw-semibold">{value}</div>
            )}
        </div>
    );
};

export default LabeledText;
