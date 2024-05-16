import torch
import os
from torch import nn, optim
from torch.utils.data import TensorDataset, DataLoader


class Lenet5(nn.Module):
    """
    for cifar10 dataset.
    """

    def __init__(self):
        super(Lenet5, self).__init__()
        self.conv_unit = nn.Sequential(
            nn.Conv2d(3, 6, kernel_size=5, padding=2),
            nn.MaxPool2d(kernel_size=2, stride=2),
            nn.Conv2d(6, 16, kernel_size=5),
            nn.MaxPool2d(kernel_size=2, stride=2),
        )
        self.fc_unit = nn.Sequential(
            nn.Linear(6*6*16, 120),
            nn.ReLU(),
            nn.Linear(120, 84),
            nn.ReLU(),
            nn.Linear(84, 10),
        )

    def forward(self, x):
        """

        :param x:
        :return:
        """
        batch = x.size(0)
        x = self.conv_unit(x)
        x = x.view(batch, 16*6*6)
        logits = self.fc_unit(x)
        return logits


def Train():
    """
    模型训练
    """
    if torch.cuda.is_available():
        device = torch.device("cuda")
        print("GPU is available.")
        print(f"Device name: {torch.cuda.get_device_name(0)}")
    else:
        device = torch.device("cpu")
        print("GPU is not available, using CPU.")

    x = torch.randn(100, 3, 32, 32)  # 假设输入图像是 32x32 的单通道图像
    labels = torch.randint(0, 10, (100,))  # 假设 10 个类别
    dataset = TensorDataset(x, labels)
    dataloader = DataLoader(dataset, batch_size=10)

    model = Lenet5().to(device)
    current_file_path = os.path.abspath(__file__)
    current_working_directory = os.path.dirname(current_file_path)
    model.load_state_dict(torch.load(os.path.join(current_working_directory, "model_state_dict.pth")))
    criteon = nn.CrossEntropyLoss().to(device)
    optimizer = optim.Adam(model.parameters(), lr=1e-3)

    for epoch in range(1):
        model.train()
        for x, label in dataloader:
            x, label = x.to(device), label.to(device)
            logits = model(x)
            loss = criteon(logits, label)
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()
    torch.save(model.state_dict(), os.path.join(current_working_directory, "model_state_dict.pth"))
    print("Model is trained successfully.")


Train()


