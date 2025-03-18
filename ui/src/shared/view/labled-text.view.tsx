import { Form } from "react-bootstrap";
import React, { ReactNode } from "react";

interface Props {
    label: string;
    value?: string | number | ReactNode;
    className?: string;
}
const LabeledText: React.FC<Props> = ({ label, value, className }) => {
    return (
        <Form.Group className={className}>
            <Form.Text muted role="label">
                {label}
            </Form.Text>
            <div>{value}</div>
        </Form.Group>
    );
};

export default LabeledText;
