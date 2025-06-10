import React, { ReactNode } from "react";
import { Form } from "react-bootstrap";

interface Props {
    label: string;
    value?: string | number | ReactNode;
    className?: string;
    onClick?: () => void;
}
const LabeledText: React.FC<Props> = ({ label, value, className, onClick }) => {
    return (
        <Form.Group className={className}>
            <Form.Text muted role="label">
                {label}
            </Form.Text>
            {onClick ? (
                <div>
                    <a onClick={onClick} href="#">
                        {value}
                    </a>
                </div>
            ) : (
                <div>{value}</div>
            )}
        </Form.Group>
    );
};

export default LabeledText;
