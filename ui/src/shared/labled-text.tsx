import { Form } from "react-bootstrap";
import React from "react";

interface Props {
    label: string;
    value?: string | number;
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
